package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AllDiseaseDTO {

    @JsonProperty("data")
    private List<DiseaseDTO> diseaseList;
    private MetaDataDTO metaData;

}
