package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.MarkerDTO;

/**
 */
public class MarkerLoadEvent {


    private MarkerDTO markerDTO;
    private String curatorZdbID ;

    public MarkerLoadEvent(MarkerDTO markerDTO,String curatorZdbID){
        this.markerDTO = markerDTO;
        this.curatorZdbID = curatorZdbID ;
    }

    public MarkerLoadEvent(MarkerDTO markerDTO){
        this.markerDTO = markerDTO;
    }

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }

    public String getCuratorZdbID() {
        return curatorZdbID;
    }
}
