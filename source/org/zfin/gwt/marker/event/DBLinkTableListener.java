package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.event.RelatedEntityChangeListener;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;

/**
 */
public abstract class DBLinkTableListener implements RelatedEntityListener<DBLinkDTO>, RelatedEntityChangeListener<DBLinkDTO> {

//    public void addDBLinkAttribution(RelatedEntityEvent<DBLinkDTO> dbLinkTableEvent) ;
//    public void updateDBLink(RelatedEntityEvent<DBLinkDTO> dbLinkTableEvent) ;


    // these methods do nothing.

    @Override
    public void addRelatedEntity(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
    }

    @Override
    public void removeRelatedEntity(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
    }

    @Override
    public void removeAttribution(RelatedEntityEvent<DBLinkDTO> dbLinkDTORelatedEntityEvent) {
    }
}