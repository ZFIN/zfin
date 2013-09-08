package org.zfin.marker.presentation;


public class DisruptorAddBean {

    public static final String NEW_DISRUPTOR_NAME = "disruptorName";
    public static final String DISRUPTOR_PUBLICATION_ZDB_ID = "disruptorPublicationZdbID";
    public static final String NEW_DISRUPTOR_COMMENT = "disruptorComment";
    public static final String NEW_DISRUPTOR_ALIAS = "disruptorAlias";
    public static final String NEW_DISRUPTOR_CURNOTE = "disruptorCuratorNote";
    public static final String  NEW_DISRUPTOR_SEQUENCE = "disruptorSequence";
    public static final String  NEW_DISRUPTOR_SECOND_SEQUENCE = "disruptorSecondSequence";

    //public static final String NEW_DISRUPTOR_TARGETGENE = "targetGeneSymbol";

    private String disruptorPublicationZdbID;
    private String disruptorName;
    private String disruptorComment;
    private String disruptorAlias;
    private String disruptorCuratorNote;
    private String disruptorType;
    private String targetGeneSymbol;
    private String disruptorSequence;
    private String disruptorSecondSequence;

    public String getDisruptorName() {
        return disruptorName;
    }

    public void setDisruptorName(String regionName) {
        this.disruptorName = regionName;
    }

    public String getDisruptorPublicationZdbID() {
        return disruptorPublicationZdbID;
    }

    public void setDisruptorPublicationZdbID(String disruptorPublicationZdbID) {
        this.disruptorPublicationZdbID = disruptorPublicationZdbID;
    }

    public String getDisruptorComment() {
        return disruptorComment;
    }

    public void setDisruptorComment(String regionComment) {
        this.disruptorComment = regionComment;
    }

    public String getDisruptorAlias() {
        return disruptorAlias;
    }

    public void setDisruptorAlias(String regionAlias) {
        this.disruptorAlias = regionAlias;
    }

    public String getDisruptorCuratorNote() {
        return disruptorCuratorNote;
    }

    public void setDisruptorCuratorNote(String regionCuratorNote) {
        this.disruptorCuratorNote = regionCuratorNote;
    }

    public String getDisruptorType() {
        return disruptorType;
    }

    public void setDisruptorType(String disruptorType) {
        this.disruptorType = disruptorType;
    }

    public String getTargetGeneSymbol() {
        return targetGeneSymbol;
    }

    public void setTargetGeneSymbol(String targetGeneSymbol) {
        this.targetGeneSymbol = targetGeneSymbol;
    }

    public String getDisruptorSequence() {
        return disruptorSequence;
    }

    public void setDisruptorSequence(String disruptorSequence) {
        this.disruptorSequence = disruptorSequence;
    }

    public String getDisruptorSecondSequence() {
        return disruptorSecondSequence;
    }

    public void setDisruptorSecondSequence(String disruptorSecondSequence) {
        this.disruptorSecondSequence = disruptorSecondSequence;
    }
}



