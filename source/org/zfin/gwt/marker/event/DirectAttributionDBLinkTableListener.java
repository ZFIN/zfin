package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.DirectAttributionTable;

/**
 * Class DirectAttributionDBLinkTableListener.
 */

public class DirectAttributionDBLinkTableListener implements DBLinkTableListener {

    private DirectAttributionTable directAttributionTable;

    public DirectAttributionDBLinkTableListener(DirectAttributionTable directAttributionTable) {
        this.directAttributionTable = directAttributionTable;
    }

    public void addDBLink(DBLinkTableEvent dbLinkTableEvent) {
        if (false == directAttributionTable.containsPublication(dbLinkTableEvent.getDBLinkDTO().getPublicationZdbID())) {
            directAttributionTable.addPublication(dbLinkTableEvent.getDBLinkDTO().getPublicationZdbID());
        }
    }

    public void addDBLinkAttribution(DBLinkTableEvent dbLinkTableEvent) {
        if (false == directAttributionTable.containsPublication(dbLinkTableEvent.getDBLinkDTO().getPublicationZdbID())) {
            directAttributionTable.addPublication(dbLinkTableEvent.getDBLinkDTO().getPublicationZdbID());
        }
    }

    public void removeDBLink(DBLinkTableEvent dbLinkTableEvent) {
    }

    public void removeDBLinkAttribution(DBLinkTableEvent dbLinkTableEvent) {
    }

    public void updateDBLink(DBLinkTableEvent dbLinkTableEvent) {
    }

} 


