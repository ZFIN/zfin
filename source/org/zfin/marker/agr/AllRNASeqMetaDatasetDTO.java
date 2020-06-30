package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.List;


public class AllRNASeqMetaDatasetDTO {

    @JsonProperty("data")
    private List<BasicRNASeqMetaDatasetDTO> datasetList;
    private MetaDataDTO metaData;


    public List<BasicRNASeqMetaDatasetDTO> getDatasetList() {
        return datasetList;
    }

    public void setDatasetList(List<BasicRNASeqMetaDatasetDTO> datasetList) {
        this.datasetList = datasetList;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
