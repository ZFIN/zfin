package org.zfin.marker.agr;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MESHDetailDTO {

    @JsonIgnore
    private String meshQualifierTerm;
    private String meshHeadingTerm;
    private String referenceId;


}
