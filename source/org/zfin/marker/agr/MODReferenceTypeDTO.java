package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class MODReferenceTypeDTO extends ZfinDTO {

    private String referenceType;
    private String source;

}