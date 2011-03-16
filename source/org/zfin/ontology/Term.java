package org.zfin.ontology;

import org.zfin.expression.Image;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Basic interface definition for a term in an ontology.
 */
public interface Term extends Comparable<Term>, Serializable {

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

//    List<Term> getParents(String relationshipType);

    void setAliases(Set<TermAlias> aliases);

//    boolean isAliasesExist();

    String getDefinition();

    void setDefinition(String definition);

    List<TermRelationship> getAllDirectlyRelatedTerms();

//    void setRelatedTerms(List<TermRelationship> relationships);

    /**
     * Retrieves all terms that are immediate children of this term.
     *
     * @return list of children terms
     */
    Set<TermRelationship> getChildTermRelationships();

    void setChildTermRelationships(Set<TermRelationship> childTerms) ;

    Set<Term> getChildTerms();

    Set<TermRelationship> getParentTermRelationships();

    void setParentTermRelationships(Set<TermRelationship> childTerms) ;

    Set<Term> getParentTerms();

//    DevelopmentStage getStart();
//
//    void setStart(DevelopmentStage stage);
//
//    DevelopmentStage getEnd();
//
//    void setEnd(DevelopmentStage stage);

    Set<Image> getImages();

    void setImages(Set<Image> images);
}
