package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.io.Serializable;

public class MarkerDBLink extends DBLink implements Comparable<MarkerDBLink>, Serializable {


    @JsonView(View.SequenceAPI.class)
    private Marker marker;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public boolean equals(Object o) {
        if (o instanceof MarkerDBLink) {
            MarkerDBLink dbLink = (MarkerDBLink) o;
//            if( getZdbID()!=null && dbLink.getZdbID().equals(getZdbID()) ){
//                return true ;
//            }

            return getMarker().getZdbID().equals(dbLink.getMarker().getZdbID())
                    &&
                    getAccessionNumber().equals(dbLink.getAccessionNumber())
                    &&
                    getReferenceDatabase().equals(dbLink.getReferenceDatabase());
        }
        return false;
    }


    public int hashCode() {
        int result = 1;
//        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
        result += (getMarker() != null ? getMarker().hashCode() : 0) * 13;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null ? getReferenceDatabase().getZdbID().hashCode() : 0) * 17;
        return result;
    }


    public String toString() {
        String returnString = "";
        returnString += getZdbID() + "\n";
        returnString += getAccessionNumber() + "\n";
        returnString += getLength() + "\n";
        returnString += getReferenceDatabase().getZdbID() + "\n";
        returnString += getMarker().getZdbID() + "\n";
        returnString += getMarker().getName() + "\n";
        return returnString;
    }

    /**
     * Sort by reference DB id, accessionNumber,and finally marker name
     *
     * @param markerDBLink MarkerDBLink to compare to.
     * @return Returns java comparison
     */
    public int compareTo(MarkerDBLink markerDBLink) {

        int refDBCompare = getReferenceDatabase().getZdbID().compareTo(markerDBLink.getReferenceDatabase().getZdbID());
        if (refDBCompare != 0) {
            return refDBCompare;
        }

        int accCompare = getAccessionNumber().compareTo(markerDBLink.getAccessionNumber());
        if (accCompare != 0) {
            return accCompare;
        }


        int markerCompare = getMarker().getZdbID().compareTo(markerDBLink.getMarker().getZdbID());
        if (markerCompare != 0) {
            return markerCompare;
        }

        return 0;
    }

}
