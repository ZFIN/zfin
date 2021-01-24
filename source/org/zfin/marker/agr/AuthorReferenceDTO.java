package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AuthorReferenceDTO extends ZfinDTO {

    private String name;
    private String referenceId;


}