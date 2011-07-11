package org.zfin.sequence;

import org.zfin.marker.Marker;

/**
 */
public class MarkerSequence {
    private String zdbID;
    private Marker marker;
    private String sequence;
    private Long offsetStart;
    private Long offsetStop;
    private String variation;

    public String getStartToOffset(){
        return sequence.substring(0,offsetStart.intValue()-1);
    }

    public String getAmbiguity(){
        return sequence.substring(offsetStart.intValue()-1,offsetStart.intValue());
    }

    public String getOffsetToEnd(){
        return sequence.substring(offsetStart.intValue());
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getLeftEnd(){
        return "5'" ;
    }

    public String getType(){
        return "Genomic" ;
    }

    public Long getOffsetStart() {
        return offsetStart;
    }

    public void setOffsetStart(Long offsetStart) {
        this.offsetStart = offsetStart;
    }

    public Long getOffsetStop() {
        return offsetStop;
    }

    public void setOffsetStop(Long offsetStop) {
        this.offsetStop = offsetStop;
    }

    public String getVariation() {
        return variation;
    }

    public void setVariation(String variation) {
        this.variation = variation;
    }
}
