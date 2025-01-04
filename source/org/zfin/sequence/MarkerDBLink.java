package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import java.io.Serializable;

@Setter
@Getter
@Entity
@DiscriminatorValue("MARK")
public class MarkerDBLink extends DBLink implements Comparable<MarkerDBLink>, Serializable {

    @JsonView(View.SequenceAPI.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dblink_linked_recid", nullable = false)
    private Marker marker;

    public boolean equals(Object o) {
        if (o instanceof MarkerDBLink dbLink) {
            return getMarker().getZdbID().equals(dbLink.getMarker().getZdbID())
                    && getAccessionNumber().equals(dbLink.getAccessionNumber())
                    && getReferenceDatabase().equals(dbLink.getReferenceDatabase());
        }
        return false;
    }

    public int hashCode() {
        int result = 1;
        result += (getMarker() != null ? getMarker().hashCode() : 0) * 13;
        result += (getAccessionNumber() != null ? getAccessionNumber().hashCode() : 0) * 19;
        result += (getReferenceDatabase() != null
                        ? getReferenceDatabase().getZdbID().hashCode()
                        : 0)
                * 17;
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

        int refDBCompare = getReferenceDatabase()
                .getZdbID()
                .compareTo(markerDBLink.getReferenceDatabase().getZdbID());
        if (refDBCompare != 0) {
            return refDBCompare;
        }

        int accCompare = getAccessionNumber().compareTo(markerDBLink.getAccessionNumber());
        if (accCompare != 0) {
            return accCompare;
        }

        int markerCompare =
                getMarker().getZdbID().compareTo(markerDBLink.getMarker().getZdbID());
        if (markerCompare != 0) {
            return markerCompare;
        }

        return 0;
    }

    public ForeignDB getReferenceDatabaseForeignDB() {
        return getReferenceDatabase().getForeignDB();
    }

    @JsonView(View.SequenceAPI.class)
    public boolean isFishMiRNA() {
        return getReferenceDatabase().getForeignDB().isFishMiRNA();
    }
}
