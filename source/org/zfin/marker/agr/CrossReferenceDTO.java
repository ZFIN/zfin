package org.zfin.marker.agr;

public class CrossReferenceDTO {

    private DataProviderDTO dataProvider;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }
}
