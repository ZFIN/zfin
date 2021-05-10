package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class STRDTO extends ZfinDTO {

    private String name;
    private List<String> targetGeneIds;
    private String soTermId;

    public String getSoTermId() {
        return soTermId;
    }

    public void setSoTermId(String soTermId) {
        this.soTermId = soTermId;
    }

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



}
