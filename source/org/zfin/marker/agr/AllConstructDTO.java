package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllConstructDTO {

    @JsonProperty("data")
    private List<ConstructDTO> constructs;
    private MetaDataDTO metaData;

    public List<ConstructDTO> getConstructs() {
        return constructs;
    }

    public void setConstructs(List<ConstructDTO> constructs) {
        this.constructs = constructs;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
