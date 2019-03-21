package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class STRDTO extends ZfinDTO {

    private String name;
    private List<String> targetGeneIds;
    private CrossReferenceDTO crossReference;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTargetGeneIds() {
        return targetGeneIds;
    }

    public void setTargetGeneIds(List<String> targetGeneIds) {
        this.targetGeneIds = targetGeneIds;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }



}
