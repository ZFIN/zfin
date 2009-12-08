package org.zfin.ontology.presentation;

/**
 * This convenience class defines a single auto complete term.
 * It consists of an anatomy term and a matching synonym.
 * A synonym is only defined in case a match is not found on the term name.
 */
public class OntologyAutoCompleteTerm {

    private String ID;
    private String termName;
    private String synonymName;

    public OntologyAutoCompleteTerm(String termName) {
        this.termName = termName;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTermName() {
        return termName;
    }

    public String getSynonymName() {
        return synonymName;
    }

    public void setSynonymName(String synonymName) {
        this.synonymName = synonymName;
    }

    public boolean isMatchOnTermName() {
        return synonymName == null;
    }
}