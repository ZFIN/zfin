package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllTranscriptDTO {

    @JsonProperty("data")
    private List<TranscriptDTO> transcripts;
    private MetaDataDTO metaData;

    public List<TranscriptDTO> getTranscripts() {
        return transcripts;
    }

    public void setTranscripts(List<TranscriptDTO> transcripts) {
        this.transcripts = transcripts;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
