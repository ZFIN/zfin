package org.zfin.ontology;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Image;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Basic interface definition for a term in an ontology.
 */
public interface Term<T extends TermRelationship> extends Comparable<Term>, Serializable {

    public static final String UNSPECIFIED = "unspecified";
    public static final String QUALITY = "quality";

    String getZdbID();

    void setZdbID(String id);

    String getTermName();

    void setTermName(String termName);

    String getOboID();

    void setOboID(String oboID);

    void setOntology(Ontology ontology);

    Ontology getOntology();

    String getComment();

    void setComment(String comment);

    boolean isObsolete();

    void setObsolete(boolean obsolete);

    boolean isRoot();

    void setRoot(boolean root);

    boolean isSecondary();

    void setSecondary(boolean secondary);

    Set<TermAlias> getAliases();

    TreeSet<TermAlias> getSortedAliases();

    void setAliases(Set<TermAlias> aliases);

    String getDefinition();

    void setDefinition(String definition);

    List<T> getAllDirectlyRelatedTerms();

    /**
     * Retrieves all terms that are immediate children of this term.
     *
     * @return list of children terms
     */
    Set<T> getChildTermRelationships();

    void setChildTermRelationships(Set<T> childTerms);

    /**
     * Retrieves all terms that are immediate children of this term
     * via a given relationship type
     *
     * @return list of children terms
     */
    List<? extends Term> getChildrenTerms(String relationshipType);

    Set<? extends Term> getChildTerms();

    Set<T> getParentTermRelationships();

    void setParentTermRelationships(Set<T> childTerms);

    Set<? extends Term> getParentTerms();

    DevelopmentStage getStart();

    DevelopmentStage getEnd();

    Set<Image> getImages();

    void setImages(Set<Image> images);

    Set<Subset> getSubsets();

    void setSubsets(Set<Subset> subsets);

    /**
     * Check if the term is part of the given sub set.
     *
     * @param subset subset
     * @return true or false
     */
    boolean isPartOfSubset(Subset subset);

    /**
     * Check if the term is part of the given sub set name.
     *
     * @param subsetName subset name
     * @return true or false
     */
    boolean isPartOfSubset(String subsetName);

    public boolean equals(Object o);

    public int hashCode();

    String getTermNameOrder();

    Set<TermDefinitionReference> getDefinitionReferences();

    void setDefinitionReferences(Set<TermDefinitionReference> references);

    boolean useForAnnotations();
}
