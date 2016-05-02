package org.zfin.ontology.repository;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.ExpressionResult2;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;

import java.util.*;

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

    int getNumberTermsForOntology(Ontology ontology);

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
    List<String> getAllRelationships(Ontology ontology);

    /**
     * Retrieve all Relationships.
     *
     * @return list of relationships
     */
    List<String> getAllRelationships();

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
     *
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
     *
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
    boolean isParentChildRelationshipExist(Term parentTerm, Term childTerm);

    List<GenericTerm> getParentDirectTerms(GenericTerm goTerm);

    List<GenericTerm> getParentTerms(GenericTerm goTerm, int distance);

    List<GenericTerm> getChildDirectTerms(GenericTerm goTerm);

    List<GenericTerm> getChildTerms(GenericTerm goTerm, int distance);

    List<GenericTerm> getAllChildTerms(GenericTerm goTerm);

    List<TransitiveClosure> getChildrenTransitiveClosures(GenericTerm term);

//    Map<String,List<TermRelationship>> getTermRelationshipsForOntology(Ontology ontology);

    Ontology getProcessOrPhysicalObjectQualitySubOntologyForTerm(Term term);

    DevelopmentStage getDevelopmentStageFromTerm(Term term);

    Map<String, TermDTO> getTermDTOsFromOntology(Ontology ontology);

    Collection<TermDTO> getTermDTOsFromOntologyNoRelation(Ontology stage);

    Set<String> getAllChildZdbIDs(String rootZdbID);

    /**
     * Retrieve all term ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     *
     * @param firstNIds number of records
     * @return list of ids
     */
    List<String> getAllTerms(int firstNIds);

    /**
     * Retrieves firstN terms of each ontology.
     * If firstN = 0 retrieve all terms
     * If firstN < 0 return null
     * <p/>
     * Note: No terms marked as secondary are retrieved (those terms are removed
     * from the obo file and only function as an alias and a place holder for a
     * previously used oboID so it does not get re-used.
     *
     * @param firstNIds number of markers to be returned
     * @return list of terms
     */
    List<String> getFirstNTermsPerOntology(int firstNIds);

    /**
     * Retrieve all terms that replace an obsoleted term.
     *
     * @param obsoletedTerm obsoleted Term
     * @return list of terms
     */
    List<ReplacementTerm> getReplacedByTerms(GenericTerm obsoletedTerm);

    /**
     * Retrieve all terms that can be considered for a given obsoleted term.
     *
     * @param obsoletedTerm obsoleted Term
     * @return list of terms
     */
    List<ConsiderTerm> getConsiderTerms(GenericTerm obsoletedTerm);

    /**
     * Retrieve phenotypes with secondary terms annotated.
     *
     * @return phenotypes
     */
    List<PhenotypeStatement> getPhenotypesOnSecondaryTerms();

    /**
     * Retrieve expressions with secondary terms annotated.
     *
     * @return expressions
     */
    List<ExpressionResult2> getExpressionsOnSecondaryTerms();

    /**
     * Retrieve go evidences with secondary terms annotated.
     *
     * @return expressions
     */
    List<MarkerGoTermEvidence> getGoEvidenceOnSecondaryTerms();

    /**
     * Retrieves a list of terms for which the start stage is not compliant
     * with the terms parent term start stage
     *
     * @return list of term Relationships
     */
    List<GenericTermRelationship> getTermsWithInvalidStartStageRange();

    /**
     * Retrieves a list of terms for which the end stage is not compliant
     * with the terms parent term end stage.
     * The termOne is the parent term while termTwo is the child term on the relationshipTerm object.
     *
     * @return list of term Relationships
     */
    List<GenericTermRelationship> getTermsWithInvalidEndStageRange();

    /**
     * Retrieves a list of term relationships of type develops_from
     * for which the start stage of the child is after the end stage of the parent term, i.e. there is no
     * stage overlap between the two terms (develops into requires a stage overlap).
     * The termOne is the parent term while termTwo is the child term on the relationshipTerm object.
     *
     * @return list of term Relationships
     */
    List<GenericTermRelationship> getTermsWithInvalidStartEndStageRangeForDevelopsFrom();

    /**
     * Retrieves all expression result objects that define stage ranges in violation of the stage ranges given
     * by the used term stages. Each term has a stage range in which it is defined, thus, the expression result
     * stage range needs to fit into the smallest window of the used terms.
     *
     * @return list of expression results records.
     */
    List<ExpressionResult> getExpressionResultsViolateStageRanges();

    /**
     * Retrieve a generic term by one or more of its values.
     *
     * @param superTerm term
     * @return term
     */
    GenericTerm getTermByExample(GenericTerm superTerm);

    /**
     * Retrieve a stage by one or more of its values.
     *
     * @param stage developmental stage
     * @return stage
     */
    DevelopmentStage getStageByExample(DevelopmentStage stage);

    /**
     * Retrieve all new relationships that were generated on a given day.
     *
     * @param date     date
     * @param ontology Ontology
     */
    List<GenericTermRelationship> getNewRelationships(Calendar date, Ontology ontology);

    /**
     * Retrieve all new relationships that were generated today.
     *
     * @param ontology Ontology
     */
    List<GenericTermRelationship> getNewRelationships(Ontology ontology);

    /**
     * Retrieve a Term Relationship by ID
     *
     * @param id relationship id
     * @return term relationship
     */
    GenericTermRelationship getRelationshipById(String id);

    /**
     * Retrieve Term Relationships that use merged terms, i.e. terms that are not used any longer.
     *
     * @return list of term relationships
     */
    List<GenericTermRelationship> getTermRelationshipsWithMergedTerms();

    /**
     * Retrieve list of distinct merged terms used in relationships.
     *
     * @return list fo terms
     */
    List<GenericTerm> getMergedTermsInTermRelationships();

    /**
     * Retrieve list of terms that are not obsoleted and not merged that do not have a defined relationship.
     *
     * @return list of terms
     */
    List<GenericTerm> getActiveTermsWithoutRelationships();

    List<GenericTerm> getTermsInSubset(String subsetName);

}
