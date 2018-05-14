package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllPhenotypeDTO {


    @JsonProperty("data")
    private List<BasicPhenotypeDTO> phenotypeList;
    private MetaDataDTO metaData;

    public List<BasicPhenotypeDTO> getPhenotypeList() {
        return phenotypeList;
    }

    public void setPhenotypeList(List<BasicPhenotypeDTO> phenotypeList) {
        this.phenotypeList = phenotypeList;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
