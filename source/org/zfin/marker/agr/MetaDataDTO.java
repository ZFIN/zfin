package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;

public class MetaDataDTO {

    private GregorianCalendar dateProduced = new GregorianCalendar();
    private List<DataProviderDTO> dataProviderList;

    @JsonSerialize(using = JsonDateSerializer.class)
    public GregorianCalendar getDateProduced() {
        return dateProduced;
    }

    public MetaDataDTO(List<DataProviderDTO> dataProviderList) {
        this.dataProviderList = dataProviderList;
    }

    public List<DataProviderDTO> getDataProvider() {
        return dataProviderList;
    }

    public void setDataProvider(List<DataProviderDTO> dataProviderList) {
        this.dataProviderList = dataProviderList;
    }

}

