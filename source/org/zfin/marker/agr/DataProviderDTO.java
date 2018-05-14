package org.zfin.marker.agr;

public class DataProviderDTO {

    private CrossReferenceDTO crossReference;
    private String type;

    public DataProviderDTO(String type, CrossReferenceDTO crossReference) {
        this.type = type;
        this.crossReference = crossReference;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
