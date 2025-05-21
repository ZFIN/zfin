package org.zfin.ontology.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.DiseaseModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.mutant.presentation.FishStatistics;
import org.zfin.ontology.*;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.NcbiOrthoExternalReference;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ForeignDB;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * This service provides a bridge between the OntologyRepository and business logic.
 */
@Log4j2
public class OntologyService {

    private final static Logger logger = LogManager.getLogger(OntologyService.class);

    private static final OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static final MarkerRepository mR = RepositoryFactory.getMarkerRepository();

    /**
     * Get the parent term that has the start stage and return
     *
     * @return stage
     */
    public static DevelopmentStage getStartStageForTerm(GenericTerm term) {
        return getStageForRelationshipType(term, RelationshipType.START_STAGE);
    }

    /**
     * Get the parent term that has the end stage and return
     */
    public static DevelopmentStage getEndStageForTerm(GenericTerm term) {
        return getStageForRelationshipType(term, RelationshipType.END_STAGE);
    }

    public static DevelopmentStage getStageForRelationshipType(GenericTerm term, RelationshipType relationshipType) {
        for (GenericTermRelationship parentTerm : term.getParentTermRelationships()) {
            if (parentTerm.getRelationshipType().equals(relationshipType)) {
                return ontologyRepository.getDevelopmentStageFromTerm(parentTerm.getTermOne());
            }
        }
        return null;
    }

    public static List<RelationshipPresentation> getRelatedTermsWithoutStages(GenericTerm term) {
        List<RelationshipPresentation> list = getRelatedTerms(term);
        List<RelationshipPresentation> newList = new ArrayList<>(list.size());
        for (RelationshipPresentation presentation : list) {
            if (!presentation.getType().equalsIgnoreCase("start stage") && !presentation.getType().equalsIgnoreCase("end stage")) {
                newList.add(presentation);
            }
        }
        return newList;
    }

    public static List<RelationshipPresentation> convertTermRelationshipsToRelationshipPresentations(GenericTerm term, List<GenericTermRelationship> termRelationships) {
        if (termRelationships == null) {
            return Collections.emptyList();
        }
        Map<String, RelationshipPresentation> types = new HashMap<>();
        for (TermRelationship rel : termRelationships) {
            String displayName;
            if (rel.getTermTwo() == null) {
                logger.error("No term two found for: " + rel.getZdbId());
            }
            if (rel.getTermTwo().equals(term)) {
                displayName = RelationshipDisplayNames.getRelationshipName(rel.getType(), true);
            } else {
                displayName = RelationshipDisplayNames.getRelationshipName(rel.getType(), false);
            }
            RelationshipPresentation presentation = types.get(displayName);
            if (presentation == null) {
                presentation = new RelationshipPresentation();
                presentation.setType(displayName);
            }
            presentation.addTerm(rel.getRelatedTerm(term));
            types.put(displayName, presentation);
        }

        return types.values().stream().collect(toList());
    }

    public static List<RelationshipPresentation> getRelatedTerms(GenericTerm term) {
        logger.debug("get related terms for " + term.getTermName());
        List<GenericTermRelationship> relatedItems = term.getAllDirectlyRelatedTerms();
        List<RelationshipPresentation> relPresentations = convertTermRelationshipsToRelationshipPresentations(term, relatedItems);
        Collections.sort(relPresentations);
        return relPresentations;
    }

    /**
     * Create a map that contains the number of terms for a given ontology from a list of terms.
     *
     * @param terms list of terms
     * @return map
     */
    public static Map<OntologyDTO, Integer> getHistogramOfTerms(List<TermDTO> terms) {
        if (terms == null) {
            return null;
        }
        Map<OntologyDTO, Integer> map = new HashMap<>(5);
        for (TermDTO term : terms) {
            Integer count = map.get(term.getOntology());
            if (count == null) {
                map.put(term.getOntology(), 0);
            }
            map.put(term.getOntology(), map.get(term.getOntology()) + 1);
        }
        return map;
    }

    public static int getNumberOfDiseaseGenes(GenericTerm term) {
        for (TermExternalReference xRef : term.getExternalReferences()) {
            if (xRef.getOmimPhenotypes() != null) {
                return xRef.getOmimPhenotypes().size();
            }
        }
        return 0;


    }

    public static Set<TermDBLink> getAGRLinks(GenericTerm term) {

        Set<TermDBLink> summaryLinks = new HashSet<>();
        for (TermDBLink termDBLink : term.getDbLinks()) {
            if (termDBLink.getReferenceDatabase().isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE)) {
                summaryLinks.add(termDBLink);
            }
        }
        return summaryLinks;
    }

    public static List<OmimPhenotypeDisplay> getOmimPhenotypeForTerm(GenericTerm term) {
        if (term == null) {
            return null;
        }
        Map<String, OmimPhenotypeDisplay> map = new HashMap<>();
        Set<TermExternalReference> termXRef = term.getExternalReferences();
        ArrayList<String> hA = new ArrayList<>();
        List<OmimPhenotypeDisplay> omimDisplaysNoOrth = new ArrayList<>();
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        for (TermExternalReference xRef : termXRef) {
            Set<OmimPhenotype> omimResults = xRef.getOmimPhenotypes();

            for (OmimPhenotype omimResult : omimResults) {
                if (omimResult.getOrtholog() != null) {
                    // form the key
                    if (omimResult.getOrtholog().getOrganism().getCommonName().startsWith("Hu")) {
                        String key = omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation() + omimResult.getName();

                        OmimPhenotypeDisplay omimDisplay;

                        // if the key is not in the map, instantiate a display (OmimPhenotypeDisplay) object and add it to the map
                        // otherwise, just get the display object from the map
                        if (!map.containsKey(key)) {
                            omimDisplay = new OmimPhenotypeDisplay();
                            map.put(key, omimDisplay);
                        } else {
                            omimDisplay = map.get(key);
                        }
                        omimDisplay.setDisease(term);
                        omimDisplay.setOrthology(omimResult.getOrtholog());
                        omimDisplay.setSymbol(omimResult.getOrtholog().getSymbol());
                        //  omimDisplay.setHumanAccession(getSequenceRepository().getDBLinkByData(omimResult.getOrtholog().getZdbID(), sequenceService.getOMIMHumanOrtholog()));
                        NcbiOtherSpeciesGene ncbiOtherGene = omimResult.getOrtholog().getNcbiOtherSpeciesGene();
                        Set<NcbiOrthoExternalReference> ncbiExternalReferenceList = ncbiOtherGene.getNcbiExternalReferenceList();
                        List<String> accessions = new ArrayList<>();
                        for (NcbiOrthoExternalReference othrRef : ncbiExternalReferenceList) {
                            if (othrRef.getReferenceDatabase().getForeignDB().getDbName() == ForeignDB.AvailableName.OMIM) {
                                accessions.add(othrRef.getAccessionNumber());
                                omimDisplay.setHomoSapiensGene(getHumanGeneDetail(othrRef.getAccessionNumber()));
                            }
                        }
                        omimDisplay.setOmimAccession(omimResult.getOmimNum());
                        omimDisplay.setName(omimResult.getName());
                        omimDisplay.setZfinGene(mR.getZfinOrtholog(omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation()));
                        if (omimResult.getOrtholog().getNcbiOtherSpeciesGene() != null) {
                            hA.add(omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation());
                            omimDisplay.setHumanGene(hA);
                        }
                    }
                } else {
                    OmimPhenotypeDisplay omimDisplayNoOrth = new OmimPhenotypeDisplay();
                    omimDisplayNoOrth.setName(omimResult.getName());
                    omimDisplayNoOrth.setHomoSapiensGene(ontologyRepository.getHumanGeneDetailById(omimResult.getHumanGeneMimNumber()));
                    omimDisplayNoOrth.setSymbol(omimDisplayNoOrth.getHomoSapiensGene().getSymbol());
                    omimDisplaysNoOrth.add(omimDisplayNoOrth);
                }
            }

        }


        // use SortedSet to hold the values of the map so that the data could be displayed in order
        List<OmimPhenotypeDisplay> omimDisplays = new ArrayList<>();

        if (map.values().size() > 0) {
            omimDisplays.addAll(map.values());
            omimDisplays.sort(new OmimPhenotypeDisplayComparator());
        }

        if (omimDisplaysNoOrth.size() > 0) {
            omimDisplaysNoOrth.sort(new OmimPhenotypeDisplayComparator());
            omimDisplays.addAll(omimDisplaysNoOrth);
        }
        return omimDisplays;
    }

    private static Map<String, HumanGeneDetail> humanGeneDetailMap;

    private static HumanGeneDetail getHumanGeneDetail(String accessionNumber) {
        if (humanGeneDetailMap == null) {
            List<HumanGeneDetail> list = getPhenotypeRepository().getHumanGeneDetailList();
            humanGeneDetailMap = list.stream().collect(toMap(HumanGeneDetail::getId, Function.identity()));
        }

        return humanGeneDetailMap.get(accessionNumber);
    }

    public static List<OmimPhenotypeDisplay> getOmimPhenotype(GenericTerm term, Pagination pagination, boolean includeChildren) {

        List<OmimPhenotypeDisplay> omimDisplays = getOmimPhenotypeForTerm(term, includeChildren);
        // apply filter elements
        List<OmimPhenotypeDisplay> filteredList = new ArrayList<>(omimDisplays);
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                List<OmimPhenotypeDisplay> tempList = new ArrayList<>();
                if (entry.getKey().startsWith("humanGeneName")) {
                    tempList = filteredList.stream().filter(display -> display.getSymbol().toLowerCase().contains(entry.getValue().toLowerCase())).toList();
                }
                if (entry.getKey().startsWith("omimName")) {
                    tempList = filteredList.stream().filter(display -> display.getName().toLowerCase().contains(entry.getValue().toLowerCase())).toList();
                }
                if (entry.getKey().startsWith("termName")) {
                    tempList = filteredList.stream().filter(display -> display.getDisease().getTermName().toLowerCase().contains(entry.getValue().toLowerCase())).toList();
                }
                if (entry.getKey().startsWith("zfinGeneName")) {
                    tempList = filteredList.stream().filter(display -> {
                        if (display.getZfinGene() == null)
                            return false;
                        String concatenatedAbbrevs = display.getZfinGene().stream().map(Marker::getAbbreviation).collect(joining(","));
                        return concatenatedAbbrevs.toLowerCase().contains(entry.getValue().toLowerCase());
                    }).toList();
                }
                filteredList = tempList;
            }
            return filteredList;
        }

        // sorting
        omimDisplays.sort(new OmimPhenotypeDisplayComparator());
        return omimDisplays;
    }

    public static List<OmimPhenotypeDisplay> getOmimPhenotypeForTerm(GenericTerm term, boolean includeChildren) {
        if (term == null) {
            return null;
        }
        if (includeChildren) {
            Set<GenericTerm> allChildren = term.getAllChildren();
            allChildren.add(term);
            return allChildren.stream().map(OntologyService::getOmimPhenotypeForTerm).flatMap(Collection::stream).collect(toList());
        } else {
            return getOmimPhenotypeForTerm(term);
        }
    }

    public static PaginationResult<OmimPhenotypeDisplay> getGenesInvolvedForDisease(GenericTerm term, Pagination pagination, boolean includeChildren) {
        if (term == null) {
            return null;
        }
        return getDiseasePageRepository().getGenesInvolved(term, pagination, includeChildren);
    }

    public static PaginationResult<FishModelDisplay> getFishDiseaseModels(GenericTerm term, Pagination pagination, boolean includeChildren) {
        if (term == null) {
            return null;
        }
        return getDiseasePageRepository().getFishDiseaseModels(term, pagination, includeChildren);
    }

    public static List<FishModelDisplay> getDiseaseModelsWithFishModel(GenericTerm disease) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(disease, false, new Pagination());
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }

        Map<FishExperiment, List<DiseaseAnnotationModel>> fishExperimentMap = modelList.stream()
            .filter(Objects::nonNull)
            .collect(groupingBy(DiseaseAnnotationModel::getFishExperiment));

        return fishExperimentMap.entrySet().stream()
            .map(entry -> {
                FishModelDisplay display = new FishModelDisplay(entry.getKey());
                display.setPublications(entry.getValue().stream()
                    .map(DiseaseAnnotationModel::getDiseaseAnnotation)
                    .map(DiseaseAnnotation::getPublication)
                    .collect(Collectors.toSet()));
                return display;
            })
            .sorted()
            .collect(Collectors.toList());
    }

    public static List<Publication> getPublicationsFromDiseaseModelsWithFishAndExperiment(GenericTerm disease, Fish fish, Experiment experiment, boolean includeChildren) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(disease, fish, includeChildren, new Pagination());
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }
        Set<Publication> publicationList = modelList.stream()
            .filter(model -> model.getFishExperiment().getExperiment().getDisplayAllConditions().equals(experiment.getDisplayAllConditions()))
            .map(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getPublication()).collect(toSet());
        Map<Fish, Map<String, Map<GenericTerm, Set<DiseaseAnnotationModel>>>> fishExperimentConditionMap = modelList.stream()
            .filter(Objects::nonNull)
            .collect(groupingBy(diseaseAnnotationModel -> diseaseAnnotationModel.getFishExperiment().getFish(), LinkedHashMap::new,
                groupingBy(annotation -> annotation.getFishExperiment().getExperiment().getDisplayAllConditions(), LinkedHashMap::new,
                    groupingBy(annotation -> annotation.getDiseaseAnnotation().getDisease(), toSet()))
            ));
        List<FishModelDisplay> list = fishExperimentConditionMap.entrySet().stream()
            .map(fishMapEntry -> {
                Fish fish1 = fishMapEntry.getKey();
                return fishMapEntry.getValue().values().stream()
                    .map(genericTermSetMap -> genericTermSetMap.entrySet().stream()
                        .map(diseaseEntrySet -> {
                            FishModelDisplay display = new FishModelDisplay(fish1);
                            display.setDisease(diseaseEntrySet.getKey());
                            display.setFishModel(diseaseEntrySet.getValue().iterator().next().getFishExperiment());
                            Set<Publication> publications = diseaseEntrySet.getValue().stream().map(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getPublication()).collect(toSet());
                            if (publications.size() == 1) {
                                display.setSinglePublication(publications.iterator().next());
                            }
                            display.setNumberOfPublications(publications.size());
                            display.setExperiment(diseaseEntrySet.getValue().iterator().next().getFishExperiment().getExperiment());
                            return display;
                        }).collect(toList())).flatMap(Collection::stream)
                    .collect(toList());
            }).flatMap(Collection::stream).sorted().toList();

        return new ArrayList<>(publicationList);
    }

    public static List<FishModelDisplay> getDiseaseModelsWithFishModelsGrouped(GenericTerm disease, boolean includeChildren, Pagination pagination) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(disease, includeChildren, pagination);
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }

        Map<Fish, Map<String, Map<GenericTerm, Set<DiseaseAnnotationModel>>>> fishExperimentConditionMap1 = modelList.stream()
            .filter(Objects::nonNull)
            .collect(groupingBy(diseaseAnnotationModel -> diseaseAnnotationModel.getFishExperiment().getFish(), LinkedHashMap::new,
                groupingBy(annotation -> annotation.getFishExperiment().getExperiment().getDisplayAllConditions(), LinkedHashMap::new,
                    groupingBy(annotation -> annotation.getDiseaseAnnotation().getDisease(), toSet()))
            ));
        return fishExperimentConditionMap1.entrySet().stream()
            .map(fishMapEntry -> {
                Fish fish = fishMapEntry.getKey();

                return fishMapEntry.getValue().entrySet().stream()
                    .map(value -> value.getValue().entrySet().stream()
                        .map(diseaseEntrySet -> {

                            FishModelDisplay display = new FishModelDisplay(fish);
                            display.setDisease(diseaseEntrySet.getKey());
                            display.setFishModel(diseaseEntrySet.getValue().iterator().next().getFishExperiment());
                            Set<Publication> publications = diseaseEntrySet.getValue().stream().map(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getPublication()).collect(toSet());
                            if (publications.size() == 1) {
                                display.setSinglePublication(publications.iterator().next());
                            }
                            display.setNumberOfPublications(publications.size());
                            display.setExperiment(diseaseEntrySet.getValue().iterator().next().getFishExperiment().getExperiment());
                            return display;
                        }).collect(toList())).flatMap(Collection::stream)
                    .collect(toList());
            }).flatMap(Collection::stream).sorted().collect(toList());
    }

    public static List<FishModelDisplay> getDiseaseModelsByFishModelsGrouped(Fish fish, Pagination pagination) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(null, fish, false, pagination);
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }

        Comparator<FishModelDisplay> diseaseConditionDisease = Comparator.comparing(display -> display.getDisease().getTermName().toLowerCase());
        Comparator<FishModelDisplay> diseaseConditionCond = Comparator.comparing(display -> display.getExperiment().getDisplayAllConditions().toLowerCase());

        Map<String, Map<GenericTerm, Set<DiseaseAnnotationModel>>> fishExperimentConditionMap1 = modelList.stream()
            .filter(Objects::nonNull)
            .collect(groupingBy(annotation -> annotation.getFishExperiment().getExperiment().getDisplayAllConditions(), LinkedHashMap::new,
                groupingBy(annotation -> annotation.getDiseaseAnnotation().getDisease(), toSet()))
            );
        return fishExperimentConditionMap1.values().stream()
            .map(genericTermSetMap -> genericTermSetMap.entrySet().stream()
                .map(diseaseEntrySet -> {
                    FishModelDisplay display = new FishModelDisplay(fish);
                    display.setDisease(diseaseEntrySet.getKey());
                    display.setFishModel(diseaseEntrySet.getValue().iterator().next().getFishExperiment());
                    Set<Publication> publications = diseaseEntrySet.getValue().stream().map(diseaseAnnotationModel -> diseaseAnnotationModel.getDiseaseAnnotation().getPublication()).collect(toSet());
                    if (publications.size() == 1) {
                        display.setSinglePublication(publications.iterator().next());
                    }
                    display.setNumberOfPublications(publications.size());
                    display.setExperiment(diseaseEntrySet.getValue().iterator().next().getFishExperiment().getExperiment());
                    return display;
                }).collect(toList())).flatMap(Collection::stream)
            .sorted(diseaseConditionDisease.thenComparing(diseaseConditionCond))
            .collect(toList());
    }


    public static String getDisplayName(GenericTerm superterm, GenericTerm subterm) {
        StringBuilder builder = new StringBuilder();
        if (superterm != null) {
            builder.append(superterm.getTermName());
        }
        if (subterm != null) {
            builder.append(" : ");
            builder.append(subterm.getTermName());
        }
        return builder.toString();
    }

    public static Collection<DiseaseModelDisplay> getDiseaseModelDisplay(Collection<DiseaseAnnotationModel> models) {
        Map<GenericTerm, Map<FishExperiment, List<Publication>>> doubleMap = models.stream()
            .collect(Collectors.groupingBy(o -> o.getDiseaseAnnotation().getDisease(),
                Collectors.groupingBy(DiseaseAnnotationModel::getFishExperiment,
                    Collectors.mapping(model -> model.getDiseaseAnnotation().getPublication(), toList()))));

        List modelDisplays = doubleMap.entrySet().stream()
            .map(genericTermMapEntry -> {
                GenericTerm term = genericTermMapEntry.getKey();
                return genericTermMapEntry.getValue().entrySet().stream()
                    .map(fishExperimentListEntry -> {
                        DiseaseModelDisplay display = new DiseaseModelDisplay();
                        display.setDisease(term);
                        display.setExperiment(fishExperimentListEntry.getKey());
                        display.setPublications(fishExperimentListEntry.getValue());
                        return display;
                    })
                    .collect(toList());
            })
            .flatMap(Collection::stream)
            .collect(toList());
        return modelDisplays;
    }

    public static boolean isPartOfSubTree(TermDTO childTerm, String rootTerm) {
        TermDTO root = OntologyManager.getInstance().getTermByID(rootTerm);
        return hasChild(root, childTerm);
    }

    public static boolean hasChild(TermDTO root, TermDTO allegedChildTerm) {
        if (allegedChildTerm.getOboID().equals(root.getOboID())) {
            return true;
        }
        GenericTerm term = getOntologyRepository().getTermByOboID(root.getOboID());
        // check the closure if the given term is a child
        List<TransitiveClosure> transitiveClosures = getOntologyRepository().getChildrenTransitiveClosures(term);
        return transitiveClosures.stream().anyMatch(closure -> closure.getChild().getOboID().equals(allegedChildTerm.getOboID()));
    }

    private Map<String, GenericTerm> diseaseTermMap = null;

    public static void fixupSearchColumns(List<OmimPhenotypeDisplay> displayListSingle) {
        displayListSingle.forEach(display -> {
            if (!CollectionUtils.isEmpty(display.getZfinGene())) {
                display.setZfinGeneSymbolSearch(display.getZfinGene().stream().map(Marker::getAbbreviation).collect(joining(",")));
            }
        });
    }

    public static PaginationResult<FishStatistics> getPhenotypeForDisease(GenericTerm term, Pagination pagination, boolean includeChildren, boolean isIncludeNormalPhenotype) {
        if (term == null) {
            return null;
        }
        PaginationResult<FishStatistics> phenotype = getDiseasePageRepository().getPhenotype(term, pagination, includeChildren, isIncludeNormalPhenotype);
        return phenotype;
    }

    public static List<ChebiFishModelDisplay> getAllChebiFishDiseaseModels(GenericTerm term, boolean includeChildren) {
        if (term == null) {
            return null;
        }
        return getDiseasePageRepository().getFishDiseaseChebiModels(term, includeChildren);
    }

    public List<GenericTerm> getRibbonStages() {
        List<GenericTerm> stageSlim = ontologyRepository.getTermsInSubset("granular_stage");
        stageSlim.sort((termA, termB) -> {
            DevelopmentStage stageA = getFirstDevelopmentStageForTerm(termA);
            if (stageA == null) {
                return 1;
            }
            DevelopmentStage stageB = getFirstDevelopmentStageForTerm(termB);
            if (stageB == null) {
                return -1;
            }
            return stageA.compareTo(stageB);
        });
        return stageSlim;
    }

    public DevelopmentStage getFirstDevelopmentStageForTerm(GenericTerm superStageTerm) {
        List<DevelopmentStage> stages = RepositoryFactory.getAnatomyRepository().getAllStagesWithoutUnknown();
        for (DevelopmentStage stage : stages) {
            if (stage.getName().toLowerCase().startsWith(superStageTerm.getTermName().toLowerCase())) {
                return stage;
            }
        }
        return null;
    }
}

