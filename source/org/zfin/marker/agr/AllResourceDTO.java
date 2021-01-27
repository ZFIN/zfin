package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllResourceDTO {

    @JsonProperty("data")
    private List<ResourceDTO> jrnl;
    private MetaDataDTO metaData;

}
