package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllReferenceDTO {

    @JsonProperty("data")
    private List<ReferenceDTO> references;
    private MetaDataDTO metaData;

}
