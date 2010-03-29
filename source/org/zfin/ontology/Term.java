package org.zfin.ontology;

import java.util.List;
import java.util.Set;

/**
 * Basic interface definition for a term in an ontology.
 */
public interface Term extends OntologyTerm{

    String getID();

    void setID(String id);

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

    void setAliases(Set<TermAlias> aliases);

    String getDefinition();

    void setDefinition(String definition);

    List<TermRelationship> getRelatedTerms();

    /**
     * Retrieves all terms that are immediate children of this term.
     * @return list of children terms
     */
    List<Term> getChildrenTerms();
}
