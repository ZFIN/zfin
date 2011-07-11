package org.zfin.ontology.presentation;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermHistogramBean implements Comparable<TermHistogramBean> {

    private String termID;
    private String termName;

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    @Override
    public int compareTo(TermHistogramBean o) {
        return termName.compareToIgnoreCase(o.getTermName());
    }
}
