package org.zfin.gwt.root.dto;

/**
 */
public class TranscriptDTO extends MarkerDTO {

    private String transcriptType;
    private String transcriptStatus;


    public TranscriptDTO copyFrom(TranscriptDTO otherTranscriptDTO) {
        transcriptType = otherTranscriptDTO.getTranscriptType();
        transcriptStatus = otherTranscriptDTO.getTranscriptStatus();
        setName(otherTranscriptDTO.getName());
        return this;
    }


    public String getTranscriptType() {
        return transcriptType;
    }

    public void setTranscriptType(String transcriptType) {
        this.transcriptType = transcriptType;
    }

    public String getTranscriptStatus() {
        return transcriptStatus;
    }

    public void setTranscriptStatus(String transcriptStatus) {
        this.transcriptStatus = transcriptStatus;
    }

}
