package org.zfin.ontology.service;

import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.ontology.TermRelationship;
import org.zfin.repository.RepositoryFactory;

import java.util.Collection;
import java.util.Map;

/**
 */
public class OntologyService {

    private final static Logger logger = Logger.getLogger(OntologyService.class);

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
                return RepositoryFactory.getOntologyRepository().getDevelopmentStageFromTerm(parentTerm.getTermOne());
            }
        }
        return null;
    }

    public static Collection<TermDTO> populateRelationships(Map<String, TermDTO> termDTOMap, OntologyManager ontologyManager){

        // pass two fills in the rest of the child / parent type info
        for(TermDTO termDTO : termDTOMap.values()){

            // populate child
            if(termDTO.getChildrenTerms()!=null){
                for(TermDTO childTerm : termDTO.getChildrenTerms()){
                    TermDTO cachedTerm = termDTOMap.get(childTerm.getZdbID()) ;
                    if(cachedTerm == null){
                        cachedTerm = ontologyManager.getTermByID(childTerm.getZdbID());
                    }

                    if(cachedTerm==null){
                        logger.error("Term is not cached, will create bad cache: "+childTerm);
                    }
                    else{
                        childTerm.shallowCopyFrom(cachedTerm);
                    }
                }
            }


            // populate parent term
            if(termDTO.getParentTerms()!=null){
                for(TermDTO parentTerm : termDTO.getParentTerms()){
                    // for the purpose of working with anatomy, the stage parent is not always in the ontology
                    TermDTO cachedTerm = termDTOMap.get(parentTerm.getZdbID()) ;
                    if(cachedTerm == null){
                        cachedTerm = ontologyManager.getTermByID(parentTerm.getZdbID());
                    }

                    if(cachedTerm==null){
                        logger.error("Term is not cached, will create bad cache: "+parentTerm);
                    }
                    else{
                        parentTerm.shallowCopyFrom(cachedTerm);

                        // handle anatomy here
                        if(parentTerm.getRelationshipType().equals(RelationshipType.START_STAGE.getDbMappedName())){
                            StageDTO stageDTO = new StageDTO();
                            stageDTO.setZdbID(parentTerm.getZdbID());
                            stageDTO.setOboID(parentTerm.getOboID());
                            stageDTO.setName(parentTerm.getName());
                            termDTO.setStartStage(stageDTO);
                        }
                        else
                        if(parentTerm.getRelationshipType().equals(RelationshipType.END_STAGE.getDbMappedName())){
                            StageDTO stageDTO = new StageDTO();
                            stageDTO.setZdbID(parentTerm.getZdbID());
                            stageDTO.setOboID(parentTerm.getOboID());
                            stageDTO.setName(parentTerm.getName());
                            termDTO.setEndStage(stageDTO);
                        }
                    }
                }
            }
        }

        return termDTOMap.values();  //To change body of implemented methods use File | Settings | File Templates.
    }

}
