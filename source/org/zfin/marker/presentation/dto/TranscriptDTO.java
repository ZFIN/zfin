package org.zfin.marker.presentation.dto;

import java.util.List;

/**
 */
public class TranscriptDTO extends MarkerDTO {

    private String transcriptType ;
    private String transcriptStatus ;



    public TranscriptDTO copyFrom(TranscriptDTO otherTranscriptDTO){
        setTranscriptType(otherTranscriptDTO.getTranscriptType());
        setTranscriptStatus(otherTranscriptDTO.getTranscriptStatus());
        setName(otherTranscriptDTO.getName());
        return this ;
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
