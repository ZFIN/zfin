package org.zfin.gwt.marker.ui;

import org.zfin.gwt.marker.event.MarkerLoadEvent;
import org.zfin.gwt.marker.event.MarkerLoadListener;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for editing markers.
 */
public abstract class AbstractMarkerEditController<T extends MarkerDTO>  extends AbstractRelatedEntityEditController<T>{


    private final List<MarkerLoadListener<T>> markerLoadListeners = new ArrayList<MarkerLoadListener<T>>();

    /**
     * Set DTO in the interface.
     * @param dto DTO to set.
     */
    void setDTO(T dto) {
        super.setDTO(dto);
        fireMarkerLoaded(new MarkerLoadEvent<T>(this.dto));
    }


    void addMarkerLoadListener(MarkerLoadListener<T> markerLoadListener) {
        markerLoadListeners.add(markerLoadListener);
    }

    void fireMarkerLoaded(MarkerLoadEvent<T> markerLoadEvent) {
        for (MarkerLoadListener<T> markerLoadListener : markerLoadListeners) {
            markerLoadListener.markerLoaded(markerLoadEvent);
        }
    }

    void synchronizeHandlesErrorListener(HandlesError handlesError) {
        addHandlesErrorListener(handlesError);
        handlesError.addHandlesErrorListener(this);
    }

}
