package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllExpressionDTO {

    @JsonProperty("data")
    private List<BasicExpressionDTO> expressionList;
    private MetaDataDTO metaData;

}
