package org.zfin.marker.presentation;


import java.util.Map;

public class SequenceTargetingReagentAddBean {

    private String publicationID;
    private String name;
    private String publicNote;
    private String alias;
    private String curatorNote;
    private String strType;
    private String targetGeneSymbol;
    private String sequence;
    private String sequence2;
    private String reportedSequence;
    private String reportedSequence2;
    private boolean reversed;
    private boolean reversed2;
    private boolean complemented;
    private boolean complemented2;
    private String supplier;

    private Map<String, String> strTypes;

    public String getName() {
        return name;
    }

    public void setName(String regionName) {
        this.name = regionName;
    }

    public String getPublicationID() {
        return publicationID;
    }

    public void setPublicationID(String publicationID) {
        this.publicationID = publicationID;
    }

    public String getPublicNote() {
        return publicNote;
    }

    public void setPublicNote(String regionComment) {
        this.publicNote = regionComment;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String regionAlias) {
        this.alias = regionAlias;
    }

    public String getCuratorNote() {
        return curatorNote;
    }

    public void setCuratorNote(String regionCuratorNote) {
        this.curatorNote = regionCuratorNote;
    }

    public String getStrType() {
        return strType;
    }

    public void setStrType(String strType) {
        this.strType = strType;
    }

    public String getTargetGeneSymbol() {
        return targetGeneSymbol;
    }

    public void setTargetGeneSymbol(String targetGeneSymbol) {
        this.targetGeneSymbol = targetGeneSymbol;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getSequence2() {
        return sequence2;
    }

    public void setSequence2(String sequence2) {
        this.sequence2 = sequence2;
    }

    public String getReportedSequence() {
        return reportedSequence;
    }

    public void setReportedSequence(String reportedSequence) {
        this.reportedSequence = reportedSequence;
    }

    public String getReportedSequence2() {
        return reportedSequence2;
    }

    public void setReportedSequence2(String reportedSequence2) {
        this.reportedSequence2 = reportedSequence2;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Map<String, String> getStrTypes() {
        return strTypes;
    }

    public void setStrTypes(Map<String, String> strTypes) {
        this.strTypes = strTypes;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean isReversed2() {
        return reversed2;
    }

    public void setReversed2(boolean reversed2) {
        this.reversed2 = reversed2;
    }

    public boolean isComplemented() {
        return complemented;
    }

    public void setComplemented(boolean complemented) {
        this.complemented = complemented;
    }

    public boolean isComplemented2() {
        return complemented2;
    }

    public void setComplemented2(boolean complemented2) {
        this.complemented2 = complemented2;
    }
}



