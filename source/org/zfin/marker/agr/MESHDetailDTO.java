package org.zfin.marker.agr;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MESHDetailDTO extends ZfinDTO  {
    private String meshQualifierTerm;
    private String meshHeadingTerm;

    public String getMeshHeadingTerm() {
        return meshHeadingTerm;
    }

    public void setMeshHeadingTerm(String meshHeadingTerm) {
        this.meshHeadingTerm = meshHeadingTerm;
    }

    public String getMeshQualifierTerm() {
        return meshQualifierTerm;
    }

    public void setMeshQualifierTerm(String meshQualifierTerm) {
        this.meshQualifierTerm = meshQualifierTerm;
    }


}
