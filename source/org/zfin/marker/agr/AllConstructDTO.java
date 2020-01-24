package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllConstructDTO {

    @JsonProperty("data")
    private List<ConstructDTO> constructs;
    private MetaDataDTO metaData;

    public List<ConstructDTO> getAlleles() {
        return constructs;
    }

    public void setConstructs(List<ConstructDTO> alleles) {
        this.constructs = constructs;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
