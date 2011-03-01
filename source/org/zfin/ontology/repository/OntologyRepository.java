package org.zfin.ontology.repository;

import org.zfin.mutant.Phenotype;
import org.zfin.ontology.*;

import java.util.List;

/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public interface OntologyRepository {

    /**
     * Retrieve a list of terms from the Quality ontology that match a given
     * string.
     *
     * @param queryString query string
     * @return list of quality terms
     */
    List<Term> getQualityTermsByQuery(String queryString);

    /**
     * Retrieve all terms from the quality ontology that are not obsoleted.
     *
     * @return list of quality terms
     */
    List<Term> getAllQualityTerms();

    /**
     * Retrieve all terms from a given ontology that are not obsoleted.
     *
     * @return list of terms
     */
    List<Term> getAllTermsFromOntology(Ontology ontology);

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
    List<TermRelationship> getAllRelationships(Ontology ontology);

    /**
     * Retrieve all Relationships.
     *
     * @return list of relationships
     */
    List<TermRelationship> getAllRelationships();

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
    List<TermRelationship> getTermRelationships(Term genericTerm);

    /**
     * @param terms
     * @return
     */
    List<TermRelationship> getTermRelationshipsForTerms(List<Term> terms);

    /**
     * Retrieve all Children terms from a given term
     *
     * @param termID ID
     * @return list of terms
     */
    List<? extends Term> getChildren(String termID);

    /**
     * Retrieve a term by name and ontology.
     *
     * @param termName term name
     * @param ontology Ontology
     * @return term
     */
    Term getTermByName(String termName, Ontology ontology);

    /**
     * Retrieve all parent/child relationships.
     *
     * @return List of transitive closure
     */
    List<TransitiveClosure> getTransitiveClosure();

    /**
     * Retrieve Term by term zdb ID.
     *
     * @param termZdbID term id
     * @return Generic Term
     */
    GenericTerm getTermByZdbID(String termZdbID);

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
     *
     * @return list of phenotypes
     */
    List<Phenotype> getPhenotypesWithSecondaryTerms();

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
    boolean isParentChildRelationshipExist(Term parentTerm, Term childTerm);

    List<Term> getParentDirectTerms(Term goTerm);

    List<Term> getParentTerms(Term goTerm, int distance);

    List<Term> getChildDirectTerms(Term goTerm);

    List<Term> getChildTerms(Term goTerm, int distance);
}
