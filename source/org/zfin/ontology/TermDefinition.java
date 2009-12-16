package org.zfin.ontology;

import org.zfin.publication.Publication;

import java.util.List;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermDefinition {

    private String ID;
    private GenericTerm term;
    private String definition;
    private List<Publication> publications;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
}
