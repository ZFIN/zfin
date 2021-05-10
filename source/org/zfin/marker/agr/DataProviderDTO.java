package org.zfin.marker.agr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataProviderDTO {

    private CrossReferenceDTO crossReference;
    private String type;

    public DataProviderDTO(String type, CrossReferenceDTO crossReference) {
        this.type = type;
        this.crossReference = crossReference;
    }


}
