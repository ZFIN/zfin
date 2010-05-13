package org.zfin.ontology.repository;

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
     * Retrieve all Children terms from a given term
     *
     * @param termID ID
     * @return list of terms
     */
    List<? extends Term> getChildren(String termID);
}
