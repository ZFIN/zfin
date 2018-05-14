package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AllGeneDTO {

    @JsonProperty("data")
    private List<GeneDTO> genes;
    private MetaDataDTO metaData;

    public List<GeneDTO> getGenes() {
        return genes;
    }

    public void setGenes(List<GeneDTO> genes) {
        this.genes = genes;
    }

    public MetaDataDTO getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaDataDTO metaData) {
        this.metaData = metaData;
    }
}
