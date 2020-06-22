package org.zfin.marker.agr;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.zfin.util.JsonDateSerializer;

import java.util.GregorianCalendar;
import java.util.List;

public class RNACentralMetaDataDTO {

    private GregorianCalendar dateProduced = new GregorianCalendar();
    private String dataProvider;
    private String schemaVersion;
    private List<String> publications;

    public List<String> getPublications() {
        return publications;
    }

    @JsonSerialize(using = JsonDateSerializer.class)
    public GregorianCalendar getDateProduced() {
        return dateProduced;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

    public void setDateProduced(GregorianCalendar dateProduced) {
        this.dateProduced = dateProduced;

    }

    public String getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(String dataProvider) {
        this.dataProvider = dataProvider;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

}

