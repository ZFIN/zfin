package org.zfin.marker.agr;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class GenotypeComponentDTO {

    private String alleleID;
    private String zygosity;

    public String getAlleleID() {
        return alleleID;
    }

    public void setAlleleID(String alleleID) {
        this.alleleID = alleleID;
    }

    public String getZygosity() {
        return zygosity;
    }

    public void setZygosity(String zygosity) {
        this.zygosity = zygosity;
    }

}
