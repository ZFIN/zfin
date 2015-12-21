package org.zfin.marker.presentation;

import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.sequence.STRMarkerSequence;

public class StrDetailsBean {

    private String zdbID;
    private String name;
    private String type;
    private String reportedSequence1;
    private String sequence1;
    private boolean reversed1;
    private boolean complemented1;
    private String reportedSequence2;
    private String sequence2;
    private boolean reversed2;
    private boolean complemented2;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReportedSequence1() {
        return reportedSequence1;
    }

    public void setReportedSequence1(String reportedSequence1) {
        this.reportedSequence1 = reportedSequence1;
    }

    public String getSequence1() {
        return sequence1;
    }

    public void setSequence1(String sequence1) {
        this.sequence1 = sequence1;
    }

    public String getReportedSequence2() {
        return reportedSequence2;
    }

    public void setReportedSequence2(String reportedSequence2) {
        this.reportedSequence2 = reportedSequence2;
    }

    public String getSequence2() {
        return sequence2;
    }

    public void setSequence2(String sequence2) {
        this.sequence2 = sequence2;
    }

    public boolean getReversed1() {
        return reversed1;
    }

    public void setReversed1(boolean reversed1) {
        this.reversed1 = reversed1;
    }

    public boolean getComplemented1() {
        return complemented1;
    }

    public void setComplemented1(boolean complemented1) {
        this.complemented1 = complemented1;
    }

    public boolean getReversed2() {
        return reversed2;
    }

    public void setReversed2(boolean reversed2) {
        this.reversed2 = reversed2;
    }

    public boolean getComplemented2() {
        return complemented2;
    }

    public void setComplemented2(boolean complemented2) {
        this.complemented2 = complemented2;
    }

    public static StrDetailsBean convert(SequenceTargetingReagent str) {
        STRMarkerSequence sequence = str.getSequence();
        StrDetailsBean bean = new StrDetailsBean();
        bean.setZdbID(str.getZdbID());
        bean.setName(str.getAbbreviation());
        bean.setType(str.getType().toString());
        bean.setReportedSequence1(sequence.getSequence());
        bean.setSequence1(sequence.getSequence());
        bean.setReportedSequence2(sequence.getSecondSequence());
        bean.setSequence2(sequence.getSecondSequence());
        return bean;
    }
}
