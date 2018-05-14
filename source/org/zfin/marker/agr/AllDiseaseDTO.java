package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllDiseaseDTO {

    @JsonProperty("data")
    private List<DiseaseDTO> diseaseList;
    private MetaDataDTO metaData;

    public List<DiseaseDTO> getDiseaseList() {
        return diseaseList;
    }

    public void setDiseaseList(List<DiseaseDTO> diseaseList) {
        this.diseaseList = diseaseList;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
