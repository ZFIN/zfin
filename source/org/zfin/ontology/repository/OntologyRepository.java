package org.zfin.ontology.repository;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public interface OntologyRepository {


    /**
     * Retrieve all terms from a given ontology that are not obsoleted.
     *
     * @return list of terms
     */
    List<GenericTerm> getAllTermsFromOntology(Ontology ontology);

    int getNumberTermsForOntology(Ontology ontology) ;
    /**
     * Retrieve a collection of aliases that match a term of a given ontology.
     * An alias has to be unique within an ontology.
     * Aliases to be considered:
     * 1) alias of terms that are not obsoleted
     * 2) alias is not of type 'secondary id'
     *
     * @param ontology Ontology
     * @return list of term aliases
     */
    List<TermAlias> getAllAliases(Ontology ontology);

    /**
     * Retrieve all Relationships for a given ontology.
     *
     * @param ontology ontology
     * @return list of relationships
     */
    List<GenericTermRelationship> getAllRelationships(Ontology ontology);

    /**
     * Retrieve all Relationships.
     *
     * @return list of relationships
     */
    List<GenericTermRelationship> getAllRelationships();

    /**
     * Retrieve Term by OBO ID.
     *
     * @param termID term id
     * @return Generic Term
     */
    GenericTerm getTermByOboID(String termID);

    /**
     * Retrieve all related Terms.
     *
     * @param genericTerm term
     * @return list of relationships
     */
    List<GenericTermRelationship> getTermRelationships(Term genericTerm);

    /**
     * @param terms
     * @return
     */
    List<GenericTermRelationship> getTermRelationshipsForTerms(List<Term> terms);

    /**
     * Retrieve all Children terms from a given term
     *
     * @param termID ID
     * @return list of terms
     */
    List<? extends Term> getChildren(String termID);

    /**
     * Retrieve a term by name and ontology.
     * Does not search for obsolete or secondary terms.
     *
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    GenericTerm getTermByNameActive(String termName, Ontology ontology);

    /**
     * Retrieve a term by name and ontology.
     *
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    GenericTerm getTermByName(String termName, Ontology ontology);

    GenericTerm getTermByName(String termName, Collection<Ontology> ontology);

    /**
     * Retrieve Term by term zdb ID.
     *
     * @param termZdbID term id
     * @return Generic Term
     */
    GenericTerm getTermByZdbID(String termZdbID);

    /**
     * Retrieve all subset definition from all ontologies in the database.
     * @return set of subsets
     */
    List<Subset> getAllSubsets();

    /**
     * Retrieve header info for all ontologies.
     *
     * @return list of headers
     */
    List<OntologyMetadata> getAllOntologyMetadata();

    /**
     * Retrieve meta data for a given ontology identified by name
     *
     * @param name ontology name
     * @return ontology metat data
     */
    OntologyMetadata getOntologyMetadata(String name);

    /**
     * Get the biggest number used in the ONTOLOGY.ont_order column.
     */
    int getMaxOntologyOrderNumber();

    /**
     * Retrieve a list of phenotypes that have annotations with secondary terms listed.
     * @return list of PhenotypeStatement
     */
    List<PhenotypeStatement> getPhenotypesWithSecondaryTerms();

    /**
     * Save a new record in the ONTOLOGY database which keeps track of versions and namespaces.
     *
     * @param metaData meta data
     */
    void saveNewDbMetaData(OntologyMetadata metaData);

    /**
     * Finds ANY parent child relationship, regardless of distance.
     *
     * @param parentTerm
     * @param childTerm
     * @return
     */
    boolean isParentChildRelationshipExist(GenericTerm parentTerm, GenericTerm childTerm);

    List<GenericTerm> getParentDirectTerms(GenericTerm goTerm);

    List<GenericTerm> getParentTerms(GenericTerm goTerm, int distance);

    List<GenericTerm> getChildDirectTerms(GenericTerm goTerm);

    List<GenericTerm> getChildTerms(GenericTerm goTerm, int distance);

    List<GenericTerm> getAllChildTerms(GenericTerm goTerm) ;

    List<TransitiveClosure> getChildrenTransitiveClosures(GenericTerm term);

//    Map<String,List<TermRelationship>> getTermRelationshipsForOntology(Ontology ontology);

    Ontology getProcessOrPhysicalObjectQualitySubOntologyForTerm(Term term);

    DevelopmentStage getDevelopmentStageFromTerm(Term term);

    Map<String,TermDTO> getTermDTOsFromOntology(Ontology ontology);

    Collection<TermDTO> getTermDTOsFromOntologyNoRelation(Ontology stage);

    Set<String> getAllChildZdbIDs(String rootZdbID);

    /**
      * Retrieve all term ids.
      * If firstNIds > 0 return only the first N.
      * If firstNIds < 0 return null
      * @param firstNIds number of records
      * @return list of ids
      */
     List<String> getAllTerms(int firstNIds);

    /**
     * Retrieves firstN terms of each ontology.
     * If firstN = 0 retrieve all terms
     * If firstN < 0 return null
     *
     * Note: No terms marked as secondary are retrieved (those terms are removed
     * from the obo file and only function as an alias and a place holder for a
     * previously used oboID so it does not get re-used.
     *
     * @param firstNIds number of markers to be returned
     * @return list of terms
     */
    List<String> getFirstNTermsPerOntology(int firstNIds);
}
