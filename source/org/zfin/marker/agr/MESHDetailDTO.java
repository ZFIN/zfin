package org.zfin.marker.agr;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MESHDetailDTO {
    private String meshQualifierTerm;
    private String meshHeadingTerm;
    private String referenceId;
    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }



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
