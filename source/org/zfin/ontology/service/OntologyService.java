package org.zfin.ontology.service;

import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.ontology.RelationshipDisplayNames;
import org.zfin.ontology.RelationshipPresentation;
import org.zfin.ontology.Term;
import org.zfin.ontology.TermRelationship;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * This service provides a bridge between the OntologyRepository and business logic.
 */
public class OntologyService {

    private final static Logger logger = Logger.getLogger(OntologyService.class);

    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    /**
     * Get the parent term that has the start stage and return
     * @return
     */
    public static DevelopmentStage getStartStageForTerm(Term term) {
        return getStageForRelationshipType(term,RelationshipType.START_STAGE);
    }

    /**
     * Get the parent term that has the end stage and return
     */
    public static DevelopmentStage getEndStageForTerm(Term term) {
        return getStageForRelationshipType(term,RelationshipType.END_STAGE);
    }

    public static DevelopmentStage getStageForRelationshipType(Term term,RelationshipType relationshipType){
        for(TermRelationship parentTerm : term.getParentTermRelationships()){
            if(parentTerm.getRelationshipType().equals(relationshipType)){
                return ontologyRepository.getDevelopmentStageFromTerm(parentTerm.getTermOne());
            }
        }
        return null;
    }

    public static List<RelationshipPresentation> getRelatedTerms(Term term) {
        logger.debug("get related terms for " + term.getTermName());
        Map<String, RelationshipPresentation> types = new HashMap<String, RelationshipPresentation>(5);
        List<TermRelationship> relatedItems = term.getAllDirectlyRelatedTerms();
        if (relatedItems != null) {
            for (TermRelationship rel : relatedItems) {
                String displayName;
                if (rel.getTermTwo() == null)
                    logger.error("No term two found for: " + rel.getZdbId());
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
        List<RelationshipPresentation> relPresentations = new ArrayList<RelationshipPresentation>(types.size());
        for (String type : types.keySet()) {
            relPresentations.add(types.get(type));
        }
        Collections.sort(relPresentations);
        return relPresentations;
    }

}
