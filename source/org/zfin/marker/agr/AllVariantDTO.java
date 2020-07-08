package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllVariantDTO {

    @JsonProperty("data")
    private List<VariantDTO> variants;
    private MetaDataDTO metaData;

}
