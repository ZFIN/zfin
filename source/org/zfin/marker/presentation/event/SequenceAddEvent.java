package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.DBLinkDTO;
import org.zfin.marker.presentation.dto.MarkerDTO;
import org.zfin.marker.presentation.dto.ReferenceDatabaseDTO;
import org.zfin.marker.presentation.dto.SequenceDTO;

/**
 */
public class SequenceAddEvent {
    private MarkerDTO markerDTO;
    private SequenceDTO sequenceDTO;
    private DBLinkDTO dbLinkDTO;
    private ReferenceDatabaseDTO referenceDatabaseDTO;

    public SequenceAddEvent() {
    }


    public SequenceAddEvent(MarkerDTO markerDTO, SequenceDTO sequenceDTO, ReferenceDatabaseDTO referenceDatabaseDTO) {
        this.markerDTO = markerDTO;
        this.sequenceDTO = sequenceDTO;
        this.referenceDatabaseDTO = referenceDatabaseDTO;
    }


    public SequenceDTO getSequenceDTO() {
        return sequenceDTO;
    }

    public void setSequenceDTO(SequenceDTO sequenceDTO) {
        this.sequenceDTO = sequenceDTO;
    }

    public DBLinkDTO getDbLinkDTO() {
        return dbLinkDTO;
    }

    public void setDbLinkDTO(DBLinkDTO dbLinkDTO) {
        this.dbLinkDTO = dbLinkDTO;
    }

    public ReferenceDatabaseDTO getReferenceDatabaseDTO() {
        return referenceDatabaseDTO;
    }

    public void setReferenceDatabaseDTO(ReferenceDatabaseDTO referenceDatabaseDTO) {
        this.referenceDatabaseDTO = referenceDatabaseDTO;
    }

    public MarkerDTO getMarkerDTO() {
        return markerDTO;
    }

    public void setMarkerDTO(MarkerDTO markerDTO) {
        this.markerDTO = markerDTO;
    }
}
