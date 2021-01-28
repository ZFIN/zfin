package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)

public class MODReferenceTypeDTO  {

    private String referenceType;
    private String tagSource;

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getSource() {
        return tagSource;
    }

    public void setSource(String source) {
        this.tagSource = source;
    }
}