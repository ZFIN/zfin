package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.MarkerDTO;

public class MarkerChangeEvent {

    private MarkerDTO markerDTO;

    public MarkerChangeEvent(MarkerDTO markerDTO){
        this.markerDTO = markerDTO;
    }

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }
}
