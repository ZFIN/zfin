package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConstructComponentDTO {

    private String componentRelation;
    protected String componentSymbol;
    protected String componentID;

}


