package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.DBLinkDTO;

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