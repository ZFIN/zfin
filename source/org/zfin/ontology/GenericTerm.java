package org.zfin.ontology;

import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class GenericTerm implements Term {

    private String ID;
    private String termName;
    private String oboID;
    private String ontologyName;
    private boolean obsolete;
    private boolean root;
    private boolean secondary;
    private String comment;
    private Set<TermAlias> synonyms;
    private String definition;

    // attribute that is populated lazily
    private List<TermRelationship> relationships;

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

    public String getOntologyName() {
        return ontologyName;
    }

    public void setOntologyName(String ontologyName) {
        this.ontologyName = ontologyName;
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

    public Set<TermAlias> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<TermAlias> synonyms) {
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
        InfrastructureRepository infrasturctureRepository = RepositoryFactory.getInfrastructureRepository();
        relationships.addAll(infrasturctureRepository.getTermRelationships(this));
        return relationships;
    }
}
