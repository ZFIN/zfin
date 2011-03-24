package org.zfin.ontology.service;

import org.apache.log4j.Logger;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.RelationshipType;
import org.zfin.gwt.root.dto.StageDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.ontology.*;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

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

    public static List<String> createSortedSynonymsFromTerm(Term term) {
        Set<TermAlias> synonyms = sortSynonyms(term);
        if (synonyms == null) {
            return null;
        }
        List<String> list = new ArrayList<String>();
        for (TermAlias synonym : synonyms) {
            list.add(synonym.getAlias());
        }
        return list;
    }


    /**
     * @param anatomyItem anatomy term
     * @return set of synonyms
     */
    private static Set<TermAlias> sortSynonyms(Term anatomyItem) {
        if (anatomyItem.getAliases() == null)
            return null;
        Set aliases = anatomyItem.getAliases();
        Set<TermAlias> synonyms = new TreeSet<TermAlias>(new SynonymSorting());
        for (Object syn : aliases) {
            TermAlias synonym = (TermAlias) syn;
            synonyms.add(synonym);
        }
        return synonyms;
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


    /**
     * Group all relationships that
     *
     * @param relationshipTypes relationship types
     * @param term              anatomy term
     * @return list of Presentation objects
     */
/*
    public static List<RelationshipPresentation> createRelationshipPresentation(Set<String> relationshipTypes,
                                                                                Term term) {
        if (relationshipTypes == null)
            return null;
        if (term == null)
            return null;

        List<RelationshipPresentation> relList = new ArrayList<RelationshipPresentation>(10);
        for (String type : relationshipTypes) {
            RelationshipPresentation rel = new RelationshipPresentation();
            List<Term> items = new ArrayList<Term>(10);
            rel.setType(type);
            for (TermRelationship relatedItem : term.getRelatedTerms()) {
                logger.debug("building term relationships: " + relatedItem.getRelationshipType().toString() + relatedItem.getRelatedTerm(term).getTermName());
                if (relatedItem.getType().equals(type)) {
                    items.add(relatedItem.getRelatedTerm(term));
                }
            }
            if (!items.isEmpty()) {
                rel.setItems(items);
                relList.add(rel);
            }
        }
        return relList;
    }
*/


    /**
     * Retrieve all terms that match a given query string across all ontologies.
     *
     * @param query query string
     * @return collection of terms
     */
    public static List<TermDTO> getMatchingTerms(String query) {
        Ontology[] ontologies = Ontology.getSerializableOntologies();
        Set<TermDTO> uniqueTerms = new HashSet<TermDTO>(10);
        for (Ontology ontology : ontologies) {
            List<TermDTO> terms = getMatchingTerms(query, ontology);
            if (terms != null)
                uniqueTerms.addAll(terms);
        }
        return new ArrayList<TermDTO>(uniqueTerms);
    }

    /**
     * Retrieve all terms that match a given query string an ontology
     *
     * @param query    query string
     * @param ontology Ontology
     * @return collection of terms
     */
    public static List<TermDTO> getMatchingTerms(String query, Ontology ontology) {
        if (ontology == null)
            return getMatchingTerms(query);
        Set<TermDTO> uniqueTerms = new HashSet<TermDTO>(10);
        MatchingTermService matcher = new MatchingTermService(0);
        Set<MatchingTerm> matchingTerms = matcher.getMatchingTerms(query, ontology);
        if (matchingTerms == null)
            return null;

        for (MatchingTerm matchingTerm : matchingTerms) {
            uniqueTerms.add(matchingTerm.getTerm());
        }
        return new ArrayList<TermDTO>(uniqueTerms);
    }

    /**
     * Inner class: Comparator that compares the alias names of the AnatomySynonym
     * and orders them alphabetically.
     */
    public static class SynonymSorting implements Comparator<TermAlias> {

        public int compare(TermAlias synOne, TermAlias synTwo) {

            int aliassig1 = synOne.getAliasGroup().getSignificance();

            int aliassig2 = synTwo.getAliasGroup().getSignificance();
            String alias = synOne.getAlias();
            String alias1 = synTwo.getAlias();

            if (aliassig1 < aliassig2)
                return -1;
            else if (aliassig1 > aliassig2)
                return 1;
            else if (aliassig1 == aliassig2)
                return alias.compareToIgnoreCase(alias1);
            else
                return 0;
        }
    }

}
