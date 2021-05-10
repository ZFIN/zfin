package org.zfin.gwt.root.ui;

import org.zfin.gwt.root.dto.NoteDTO;

/**
 * A factory class for creating notes by type.
 */
public class IntegratedNoteEntryFactory {


    public static AbstractNoteEntry createIntegratedNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {

        switch (noteDTO.getNoteEditMode()){
            case PUBLIC:
                return new PublicNoteEntry(noteDTO, handlesError);
            case PRIVATE:
                return new PrivateNoteEntry(noteDTO, handlesError);
            case EXTERNAL:
                return new ExternalNoteEntry(noteDTO, handlesError);
            default:
                return null ;
        }
    }

}
