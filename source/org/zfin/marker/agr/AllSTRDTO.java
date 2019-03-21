package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllSTRDTO {

    @JsonProperty("data")
    private List<STRDTO> strs;
    private MetaDataDTO metaData;

    public List<STRDTO> getSTRs() {
        return strs;
    }

    public void setSTRs(List<STRDTO> strs) {
        this.strs = strs;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
