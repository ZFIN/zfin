package org.zfin.sequence;

import jakarta.persistence.*;
import org.zfin.marker.Transcript;

import java.io.Serializable;

@Entity
@DiscriminatorValue("TSCR")
public class TranscriptDBLink extends DBLink implements Comparable<TranscriptDBLink>, Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dblink_linked_recid", nullable = false)
    private Transcript transcript;

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    public boolean equals(Object o) {
        if (o instanceof TranscriptDBLink dbLink) {
            if (dbLink.getTranscript().getZdbID().equals(dbLink.getTranscript().getZdbID())
                    && dbLink.getAccessionNumber().equals(dbLink.getAccessionNumber())
                    && dbLink.getReferenceDatabase().equals(dbLink.getReferenceDatabase())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        int result = 1;
        result += (getTranscript() != null ? getTranscript().hashCode() : 0) * 13;
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
        returnString += getTranscript().getZdbID() + "\n";
        returnString += getTranscript().getName() + "\n";
        return returnString;
    }

    /**
     * Sort by  reference DB id, accessionNumber, and finally marker name
     *
     * @param transcriptDBLink
     * @return Java object comparison
     */
    public int compareTo(TranscriptDBLink transcriptDBLink) {

        int refDBCompare = getReferenceDatabase()
                .getZdbID()
                .compareTo(transcriptDBLink.getReferenceDatabase().getZdbID());
        if (refDBCompare != 0) {
            return refDBCompare;
        }

        int accCompare = getAccessionNumber().compareTo(transcriptDBLink.getAccessionNumber());
        if (accCompare != 0) {
            return accCompare;
        }

        int markerCompare = getTranscript()
                .getZdbID()
                .compareTo(transcriptDBLink.getTranscript().getZdbID());
        if (markerCompare != 0) {
            return markerCompare;
        }

        return 0;
    }
}
