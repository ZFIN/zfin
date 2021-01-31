package org.zfin.marker.agr;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import java.util.List;


@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ReferenceExchangeDTO {
    private String pubMedId;
    private String allianceCategory;
    private String modId;
    private List<ReferenceTagDTO> tags;
    private List<MODReferenceTypeDTO> MODReferenceTypes;


}

