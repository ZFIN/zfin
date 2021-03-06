package org.zfin.ontology.service;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.presentation.DiseaseModelDisplay;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.*;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.NcbiOrthoExternalReference;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ForeignDB;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * This service provides a bridge between the OntologyRepository and business logic.
 */
@Log4j2
public class OntologyService {

    private final static Logger logger = LogManager.getLogger(OntologyService.class);

    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static MarkerRepository mR = RepositoryFactory.getMarkerRepository();
    private static OrthologyRepository oR = RepositoryFactory.getOrthologyRepository();

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

    public static List<RelationshipPresentation> getRelatedTerms(GenericTerm term) {
        logger.debug("get related terms for " + term.getTermName());
        Map<String, RelationshipPresentation> types = new HashMap<>(5);
        List<GenericTermRelationship> relatedItems = term.getAllDirectlyRelatedTerms();
        if (relatedItems != null) {
            for (TermRelationship rel : relatedItems) {
                String displayName;
                if (rel.getTermTwo() == null) {
                    logger.error("No term two found for: " + rel.getZdbId());
                }
                if (rel.getTermTwo().equals(term)) {
                    displayName = RelationshipDisplayNames.getRelationshipName(rel.getType(), true);
                } else {
                    displayName = RelationshipDisplayNames.getRelationshipName(rel.getType(), false);
                }
                logger.debug("displayName: " + displayName);
                RelationshipPresentation presentation = types.get(displayName);
                if (presentation == null) {
                    presentation = new RelationshipPresentation();
                    presentation.setType(displayName);
                }
                presentation.addTerm(rel.getRelatedTerm(term));
                types.put(displayName, presentation);
            }
        } else {
            logger.debug("term has no RelatedTerms");
        }
        List<RelationshipPresentation> relPresentations = new ArrayList<>(types.size());
        for (String type : types.keySet()) {
            relPresentations.add(types.get(type));
        }
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

                        OmimPhenotypeDisplay omimDisplay = new OmimPhenotypeDisplay();

                        // if the key is not in the map, instantiate a display (OmimPhenotypeDisplay) object and add it to the map
                        // otherwise, just get the display object from the map
                        if (!map.containsKey(key)) {
                            omimDisplay = new OmimPhenotypeDisplay();
                            map.put(key, omimDisplay);
                        } else {
                            omimDisplay = map.get(key);
                        }
                        omimDisplay.setOrthology(omimResult.getOrtholog());
                        omimDisplay.setSymbol(omimResult.getOrtholog().getSymbol());
                        //  omimDisplay.setHumanAccession(getSequenceRepository().getDBLinkByData(omimResult.getOrtholog().getZdbID(), sequenceService.getOMIMHumanOrtholog()));
                        NcbiOtherSpeciesGene ncbiOtherGene = omimResult.getOrtholog().getNcbiOtherSpeciesGene();
                        Set<NcbiOrthoExternalReference> ncbiExternalReferenceList = ncbiOtherGene.getNcbiExternalReferenceList();
                        List<String> accessions = new ArrayList<>();
                        for (NcbiOrthoExternalReference othrRef : ncbiExternalReferenceList) {
                            if (othrRef.getReferenceDatabase().getForeignDB().getDbName() == ForeignDB.AvailableName.OMIM) {
                                accessions.add(othrRef.getAccessionNumber());
                            }
                        }
                        omimDisplay.setOmimAccession(accessions.get(0));
                        omimDisplay.setName(omimResult.getName());
                        omimDisplay.setOmimNum(omimResult.getOmimNum());
                        omimDisplay.setZfinGene(mR.getZfinOrtholog(omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation()));
                        if (omimResult.getOrtholog().getNcbiOtherSpeciesGene() != null) {
                            hA.add(omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation());
                            omimDisplay.setHumanGene(hA);
                        }
                    }
                } else {
                    OmimPhenotypeDisplay omimDisplayNoOrth = new OmimPhenotypeDisplay();
                    omimDisplayNoOrth.setName(omimResult.getName());
                    omimDisplayNoOrth.setHumanGeneDetail(ontologyRepository.getHumanGeneDetailById(omimResult.getHumanGeneMimNumber()));
                    omimDisplayNoOrth.setOmimNum(omimResult.getOmimNum());
                    omimDisplayNoOrth.setSymbol(omimDisplayNoOrth.getHumanGeneDetail().getGeneSymbol());
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

    public static List<FishModelDisplay> getDiseaseModelsWithFishModel(GenericTerm disease) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(disease);
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

    @Nullable
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

