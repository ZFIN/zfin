package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllFishDTO {

    @JsonProperty("data")
    private List<FishDTO> fishes;
    private MetaDataDTO metaData;

    public List<FishDTO> getFishes() {
        return fishes;
    }

    public void setFishes(List<FishDTO> fishes) {
        this.fishes = fishes;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
