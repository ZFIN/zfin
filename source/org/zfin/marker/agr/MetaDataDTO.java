package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;

public class MetaDataDTO {

    private GregorianCalendar dateProduced = new GregorianCalendar();
    private String dataProvider;

    public MetaDataDTO(String dataProvider) {
        this.dataProvider = dataProvider;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public GregorianCalendar getDateProduced() {
        return dateProduced;
    }


    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }
}

