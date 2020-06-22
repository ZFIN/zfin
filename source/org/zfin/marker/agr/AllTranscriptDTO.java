package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllTranscriptDTO {

    @JsonProperty("data")
    private List<TranscriptDTO> transcripts;
    private RNACentralMetaDataDTO metaData;

    public List<TranscriptDTO> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(List<TranscriptDTO> transcripts) {
        this.transcripts = transcripts;
    }

    public RNACentralMetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(RNACentralMetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
