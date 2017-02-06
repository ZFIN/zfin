package org.zfin.marker.agr;

public class DataProviderDTO {

    private String taxonId = "7955";
    private String dataProvider;

    public DataProviderDTO(String name) {
        this.dataProvider = name;
    }

    public String getTaxonId() {
        return taxonId;
    }

    public String getDataProvider() {
        return dataProvider;
    }

}
