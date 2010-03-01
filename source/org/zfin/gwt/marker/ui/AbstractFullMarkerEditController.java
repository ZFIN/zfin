package org.zfin.gwt.marker.ui;

import org.zfin.gwt.marker.event.DirectAttributionAddsRelatedEntityListener;
import org.zfin.gwt.root.dto.MarkerDTO;

/**
 * Base class of full marker edit controller.
 */
public abstract class AbstractFullMarkerEditController<T extends MarkerDTO> extends AbstractMarkerEditController<T>{


    // gui elements
    final DirectAttributionTable directAttributionTable = new HandledDirectAttributionTable();
    final PreviousNamesBox previousNamesBox = new PreviousNamesBox();
    final PublicationLookupBox publicationLookupBox = new PublicationLookupBox();
    final MarkerNoteBox<T> markerNoteBox = new MarkerNoteBox<T>() ;


    protected void addListeners() {

        // direct attribution listeners
        directAttributionTable.addDirectAttributionListener(publicationLookupBox);
        previousNamesBox.addRelatedEntityCompositeListener(
                new DirectAttributionAddsRelatedEntityListener(directAttributionTable));


        // publication change listeners
        publicationLookupBox.addPublicationChangeListener(previousNamesBox);
        publicationLookupBox.addPublicationChangeListener(directAttributionTable);
        publicationLookupBox.addPublicationChangeListener(this);

        // add error handler listeners
        synchronizeHandlesErrorListener(previousNamesBox);
        synchronizeHandlesErrorListener(directAttributionTable);
    }

}
