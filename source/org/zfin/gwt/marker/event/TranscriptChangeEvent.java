package org.zfin.gwt.marker.event;

import org.zfin.gwt.root.dto.TranscriptDTO;

/**
 * Event gets fired when a name is changed.
 */
public class TranscriptChangeEvent extends MarkerChangeEvent<TranscriptDTO>{

    public TranscriptChangeEvent(TranscriptDTO transcriptDTO,String previousName) {
        super(transcriptDTO,previousName);
    }

}
