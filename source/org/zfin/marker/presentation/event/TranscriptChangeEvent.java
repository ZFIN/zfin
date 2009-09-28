package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.dto.MarkerDTO;
import org.zfin.marker.presentation.dto.TranscriptDTO;

/**
 * Event gets fired when a name is changed.
 */
public class TranscriptChangeEvent {

    public static final String ADD_PUBLICATION = "ADD_PUBLICATION" ;
    public static final String REMOVE_PUBLICATION = "REMOVE_PUBLICATION" ; 

    private TranscriptDTO transcriptDTO ; 

    public TranscriptChangeEvent(TranscriptDTO transcriptDTO){
        this.transcriptDTO = transcriptDTO ;
    }

    public TranscriptDTO getTranscriptDTO() {
        return transcriptDTO;
    }

}
