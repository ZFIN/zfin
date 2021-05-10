package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.MarkerDTO;

/**
 */
public class MarkerLoadEvent<T extends MarkerDTO> {


    private T markerDTO;
    private String curatorZdbID ;

    public MarkerLoadEvent(T markerDTO,String curatorZdbID){
        this.markerDTO = markerDTO;
        this.curatorZdbID = curatorZdbID ;
    }

    public MarkerLoadEvent(T markerDTO){
        this.markerDTO = markerDTO;
    }

    public T getMarkerDTO() {
        return markerDTO;
    }
}
