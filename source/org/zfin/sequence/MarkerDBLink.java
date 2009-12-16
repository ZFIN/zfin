package org.zfin.sequence;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;

import java.io.Serializable;

public class MarkerDBLink extends DBLink implements Comparable<MarkerDBLink>, Serializable {


    Logger logger = Logger.getLogger(MarkerDBLink.class);

    private Marker marker;
//    private Accession referencingAccession ;

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

            if (dbLink.getMarker().getZdbID().equals(dbLink.getMarker().getZdbID())
                    &&
                    dbLink.getAccessionNumber().equals(dbLink.getAccessionNumber())
                    &&
                    dbLink.getReferenceDatabase().equals(dbLink.getReferenceDatabase())
                    ) {
                return true;
            }
        }
        return false;
    }


    public int hashCode() {
        int result = 1;
//        result += (getZdbID() != null ? getZdbID().hashCode() : 0) * 29;
        result += (getMarker() != null ? getMarker().hashCode() : 0) * 13;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null ? getReferenceDatabase().hashCode() : 0) * 17;
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
     * Sort by accessionNBumber, reference DB id, and finally marker name
     *
     * @param markerDBLink MarkerDBLink to compare to.
     * @return Returns java comparison
     */
    public int compareTo(MarkerDBLink markerDBLink) {

        int accCompare = getAccessionNumber().compareTo(markerDBLink.getAccessionNumber());
        if (accCompare != 0) {
            return accCompare;
        }

        int refDBCompare = getReferenceDatabase().getZdbID().compareTo(markerDBLink.getReferenceDatabase().getZdbID());
        if (refDBCompare != 0) {
            return refDBCompare;
        }

        int markerCompare = getMarker().getZdbID().compareTo(markerDBLink.getMarker().getZdbID());
        if (markerCompare != 0) {
            return markerCompare;
        }

        return 0;
    }

}
