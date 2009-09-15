package org.zfin.anatomy.presentation;

/**
 * This convenience class defines a single auto complete term.
 * It consists of an anatomy term and a matching synonym.
 * A synonym is only defined in case a match is not found on the term name.
 */
public class AnatomyAutoCompleteTerm {

    private String termName;
    private String synonymName;

    public AnatomyAutoCompleteTerm(String termName) {
        this.termName = termName;
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
