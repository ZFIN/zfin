package org.zfin.ontology.service;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.feature.Feature;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.presentation.FishModelDisplay;
import org.zfin.ontology.*;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.NcbiOrthoExternalReference;
import org.zfin.orthology.NcbiOtherSpeciesGene;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.FeatureDBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.service.SequenceService;

import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * This service provides a bridge between the OntologyRepository and business logic.
 */
public class OntologyService {

    private final static Logger logger = Logger.getLogger(OntologyService.class);

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
        SequenceService sequenceService = new SequenceService();

        if (term == null) {
            return null;
        }
        Map<String, OmimPhenotypeDisplay> map = new HashMap<>();
        Set<TermExternalReference> termXRef = term.getExternalReferences();
        ArrayList<String> hA = new ArrayList<>();
        for (TermExternalReference xRef : termXRef) {
            Set<OmimPhenotype> omimResults = xRef.getOmimPhenotypes();

            for (OmimPhenotype omimResult : omimResults) {
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
                    omimDisplay.setOrthology(omimResult.getOrtholog());
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
                    if (omimResult.getOrtholog() != null) {

                        hA.add(omimResult.getOrtholog().getNcbiOtherSpeciesGene().getAbbreviation());
                        omimDisplay.setHumanGene(hA);
                    }
                }
            }

        }


        // use SortedSet to hold the values of the map so that the data could be displayed in order
        List<OmimPhenotypeDisplay> omimDisplays = new ArrayList<>();

        if (map.values().size() > 0) {
            omimDisplays.addAll(map.values());
        }
        omimDisplays.sort(new OmimPhenotypeDisplayComparator());


        return omimDisplays;
    }

    public static List<FishModelDisplay> getDiseaseModelsWithFishModel(GenericTerm disease) {
        List<DiseaseAnnotationModel> modelList = getPhenotypeRepository().getHumanDiseaseModels(disease);
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }
        Map<FishExperiment, FishModelDisplay> map = new HashMap<>();
        for (DiseaseAnnotationModel model : modelList) {
            // ignore disease models without fish models
            if (model == null) {
                continue;
            }

            FishModelDisplay display = new FishModelDisplay(model.getFishExperiment());
            display.addPublication(model.getDiseaseAnnotation().getPublication());

            FishModelDisplay mapModel = map.get(model.getFishExperiment());
            if (mapModel == null) {
                map.put(model.getFishExperiment(), display);
            } else {
                mapModel.addPublication(model.getDiseaseAnnotation().getPublication());
            }
        }
        List<FishModelDisplay> displayList = new ArrayList<>(map.values());
        Collections.sort(displayList);
        return displayList;
    }


    public static String getDisplayName(GenericTerm superterm, GenericTerm subterm) {
        StringBuilder builder = new StringBuilder();
        if (superterm != null)
            builder.append(superterm.getTermName());
        if (subterm != null) {
            builder.append(" : ");
            builder.append(subterm.getTermName());
        }
        return builder.toString();
    }
}

