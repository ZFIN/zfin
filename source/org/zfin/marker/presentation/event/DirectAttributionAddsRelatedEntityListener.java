package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.client.DirectAttributionTable;
import org.zfin.marker.presentation.dto.RelatedEntityDTO;

/**
 * Class DirectAttributionAddsRelatedEntityListener.
 */
public class DirectAttributionAddsRelatedEntityListener<U extends RelatedEntityDTO> implements RelatedEntityListener<U> {

    private DirectAttributionTable directAttributionTable;

    public DirectAttributionAddsRelatedEntityListener(DirectAttributionTable directAttributionTable) {
        this.directAttributionTable = directAttributionTable;
    }

    public void addRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
        addPublication(relatedEntityEvent.getRelatedEntityDTO().getPublicationZdbID());
    }

    public void addAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
        addPublication(relatedEntityEvent.getRelatedEntityDTO().getPublicationZdbID());
    }

    public void removeRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
    }

    public void removeAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
    }

    protected void addPublication(String publicationZdbID) {
        if (publicationZdbID != null && publicationZdbID.length() >= 16 &&
                false == directAttributionTable.containsPublication(publicationZdbID)) {
            directAttributionTable.addPublication(publicationZdbID);
        }
    }
} 


