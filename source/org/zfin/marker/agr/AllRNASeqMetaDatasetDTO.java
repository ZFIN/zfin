package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.List;


@Getter
@Setter
public class AllRNASeqMetaDatasetDTO {

    @JsonProperty("data")
    private List<BasicRNASeqMetaDatasetDTO> datasetList;
    private MetaDataDTO metaData;

}
