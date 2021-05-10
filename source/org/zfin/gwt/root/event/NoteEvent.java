package org.zfin.gwt.root.event;

import org.zfin.gwt.root.dto.NoteDTO;

/**
 */
public class NoteEvent {

    private NoteDTO noteDTO ;

    public NoteEvent(NoteDTO noteDTO){
        this.noteDTO = noteDTO ;
    }

    public NoteDTO getNoteDTO() {
        return noteDTO;
    }
}
