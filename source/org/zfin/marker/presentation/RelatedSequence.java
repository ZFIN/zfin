package org.zfin.marker.presentation;

import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;

import java.util.List;


public class RelatedSequence {
    private MarkerDBLink dblink;
    private String typeColumn;
    private String lengthColumn;

    public MarkerDBLink getDblink() {
        return dblink;
    }

    public void setDblink(MarkerDBLink dblink) {
        this.dblink = dblink;
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn;
    }

    public String getLengthColumn() {
        return lengthColumn;
    }

    public void setLengthColumn(String lengthColumn) {
        this.lengthColumn = lengthColumn;
    }
}
