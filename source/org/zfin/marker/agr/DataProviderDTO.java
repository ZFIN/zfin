package org.zfin.marker.agr;

public class DataProviderDTO {

    private String taxonID = "7955";
    private String name;

    public DataProviderDTO(String name) {
        this.name = name;
    }

    public String getTaxonID() {
        return taxonID;
    }

    public String getName() {
        return name;
    }

}
