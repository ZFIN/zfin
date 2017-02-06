package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.Date;

public class MetaDataDTO {

    private Date dateProduced = new Date();
    private DataProviderDTO dataProvider;

    @JsonSerialize(using = JsonDateSerializer.class)
    public Date getDateProduced() {
        return dateProduced;
    }

    public void setDateProduced(Date dateProduced) {
        this.dateProduced = dateProduced;
    }

    public DataProviderDTO getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(DataProviderDTO dataProvider) {
        this.dataProvider = dataProvider;
    }
}
