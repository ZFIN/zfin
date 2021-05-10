package org.zfin.publication;

public class MeshHeadingTerm implements Comparable<MeshHeadingTerm> {

    private MeshTerm term;
    private Boolean majorTopic;

    public MeshTerm getTerm() {
        return term;
    }

    public void setTerm(MeshTerm term) {
        this.term = term;
    }

    public Boolean getMajorTopic() {
        return majorTopic;
    }

    public void setMajorTopic(Boolean isMajorTopic) {
        this.majorTopic = isMajorTopic;
    }

    @Override
    public String toString() {
        String str = term.getName();
        if (majorTopic) {
            str += "*";
        }
        return str;
    }

    @Override
    public int compareTo(MeshHeadingTerm o) {
        return term.compareTo(o.getTerm());
    }

}
