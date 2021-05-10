package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.DirectAttributionTable;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;
import org.zfin.gwt.root.ui.PublicationValidator;

/**
 * Class DirectAttributionAddsRelatedEntityListener.
 */
public class DirectAttributionAddsRelatedEntityListener<U extends RelatedEntityDTO> implements RelatedEntityListener<U> {

    private DirectAttributionTable directAttributionTable;
    private PublicationValidator publicationValidator = new PublicationValidator();

    public DirectAttributionAddsRelatedEntityListener(DirectAttributionTable directAttributionTable) {
        this.directAttributionTable = directAttributionTable;
    }

    public void addRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
        addPublication(relatedEntityEvent.getDTO().getPublicationZdbID());
    }

    public void addAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
        addPublication(relatedEntityEvent.getDTO().getPublicationZdbID());
    }

    public void removeRelatedEntity(RelatedEntityEvent<U> relatedEntityEvent) {
    }

    public void removeAttribution(RelatedEntityEvent<U> relatedEntityEvent) {
    }

    protected void addPublication(String publicationZdbID) {
        if (false == directAttributionTable.containsPublication(publicationZdbID)) {
            if (publicationValidator.validate(publicationZdbID, directAttributionTable)) {
                directAttributionTable.addPublication(publicationZdbID);
            }
        }
    }
} 


