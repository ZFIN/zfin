package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.DirectAttributionTable;
import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;

/**
 * Class DirectAttributionDBLinkTableListener.
 */

public class DirectAttributionDBLinkTableListener extends DBLinkTableListener {

    private DirectAttributionTable directAttributionTable;

    public DirectAttributionDBLinkTableListener(DirectAttributionTable directAttributionTable) {
        this.directAttributionTable = directAttributionTable;
    }

    @Override
    public void addAttribution(RelatedEntityEvent<DBLinkDTO> event) {
        if (false == directAttributionTable.containsPublication(event.getDTO().getPublicationZdbID())) {
            directAttributionTable.addPublication(event.getDTO().getPublicationZdbID());
        }
    }

    @Override
    public void dataChanged(RelatedEntityEvent<DBLinkDTO> dataChangedEvent) {
    }


} 


