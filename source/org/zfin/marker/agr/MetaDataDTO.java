package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;

public class MetaDataDTO {

    private GregorianCalendar dateProduced = new GregorianCalendar();
    private DataProviderDTO dataProvider;

    @JsonSerialize(using = JsonDateSerializer.class)
    public GregorianCalendar getDateProduced() {
        return dateProduced;
    }

    public MetaDataDTO(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }

    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }

}

