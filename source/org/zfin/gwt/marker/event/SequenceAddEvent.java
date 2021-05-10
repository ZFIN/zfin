package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.DBLinkDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.dto.SequenceDTO;

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
