package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;

public class MetaDataDTO {

    private GregorianCalendar dateProduced = new GregorianCalendar();
    private DataProviderDTO dataProvider;
    private String loadingURI = "http://zfin.org";


    @JsonSerialize(using = JsonDateSerializer.class)
    public GregorianCalendar getDateProduced() {
        return dateProduced;
    }

    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String getLoadingURI() {
        return loadingURI;
    }
}

