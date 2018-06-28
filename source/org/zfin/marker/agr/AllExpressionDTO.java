package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllExpressionDTO {

    @JsonProperty("data")
    private List<BasicExpressionDTO> expressionList;
    private MetaDataDTO metaData;

    public List<BasicExpressionDTO> getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(List<BasicExpressionDTO> expressionList) {
        this.expressionList = expressionList;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
