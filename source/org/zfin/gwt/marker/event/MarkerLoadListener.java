package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.MarkerDTO;

/**
 */
public interface MarkerLoadListener<T extends MarkerDTO> {
    void markerLoaded(MarkerLoadEvent<T> markerLoadEvent) ;
}
