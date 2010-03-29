package org.zfin.ontology;

import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class GenericTerm implements Term, Serializable {

    public static final String QUALITY = "quality";
    
    private String ID;
    private String termName;
    private String oboID;
    private String ontologyName;
    private Ontology ontology;
    private boolean obsolete;
    private boolean root;
    private boolean secondary;
    private String comment;
    private Set<TermAlias> synonyms;
    private String definition;

    // attribute that is populated lazily
    private List<TermRelationship> relationships;
    private List<Term> children;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getOboID() {
        return oboID;
    }

    public void setOboID(String oboID) {
        this.oboID = oboID;
    }

    public void setOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    public Ontology getOntology() {
        return ontology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isSecondary() {
        return secondary;
    }

    public void setSecondary(boolean secondary) {
        this.secondary = secondary;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<TermAlias> getAliases() {
        return synonyms;
    }

    public void setAliases(Set<TermAlias> synonyms) {
        this.synonyms = synonyms;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<TermRelationship> getRelatedTerms() {
        if (relationships != null)
            return relationships;

        relationships = new ArrayList<TermRelationship>();
        OntologyRepository ontolgoyOntologyRepository = RepositoryFactory.getOntologyRepository();
        relationships.addAll(ontolgoyOntologyRepository.getTermRelationships(this));
        return relationships;
    }

    public List<Term> getChildrenTerms() {
        if (children != null)
            return children;

        children = new ArrayList<Term>();
        OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
        children.addAll(ontologyRepository.getChildren(ID));
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericTerm genericTerm = (GenericTerm) o;

        if (termName != null ? !termName.equals(genericTerm.termName) : genericTerm.termName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return termName != null ? termName.hashCode() : 0;
    }
}
