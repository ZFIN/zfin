package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllGeneDTO {

    @JsonProperty("data")
    private List<GeneDTO> genes;
    private MetaDataDTO metaData;

}
