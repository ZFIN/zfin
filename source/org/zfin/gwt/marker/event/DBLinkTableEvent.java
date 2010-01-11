package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.DBLinkDTO;

/**
 */
public class DBLinkTableEvent {

    private DBLinkDTO dbLinkDTO;


    public DBLinkTableEvent(DBLinkDTO dbLinkDTO){
        this.dbLinkDTO = dbLinkDTO;
    }


    public DBLinkDTO getDBLinkDTO() {
        return dbLinkDTO;
    }

}