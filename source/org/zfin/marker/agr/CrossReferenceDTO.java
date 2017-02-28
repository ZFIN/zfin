package org.zfin.marker.agr;

public class CrossReferenceDTO {

    private String dataProvider;
    private String id;

    public CrossReferenceDTO(String dataProvider, String id) {
        this.dataProvider = dataProvider;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDataProvider() {
        return dataProvider;
    }
}
