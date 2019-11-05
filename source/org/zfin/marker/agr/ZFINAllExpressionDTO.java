package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ZFINAllExpressionDTO {
    @JsonProperty("data")
    private List<ZFINExpressionDTO> expressionList;
    private MetaDataDTO metaData;

    public List<ZFINExpressionDTO> getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(List<ZFINExpressionDTO> expressionList) {
        this.expressionList = expressionList;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
