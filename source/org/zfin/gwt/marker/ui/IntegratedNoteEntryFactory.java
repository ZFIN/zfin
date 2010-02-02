package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.HandlesError;

/**
 * A factory class for creating notes by type.
 */
class IntegratedNoteEntryFactory {


    public static AbstractNoteEntry createIntegratedNoteEntry(NoteDTO noteDTO, HandlesError handlesError){
        AbstractNoteEntry noteEntry = null ;

        if(noteDTO.getEditMode().equals(NoteBox.EditMode.PUBLIC.name())){
            noteEntry = new PublicNoteEntry(noteDTO,handlesError) ;
        }
        else
        if(noteDTO.getEditMode().equals(NoteBox.EditMode.PRIVATE.name())){
            noteEntry = new PrivateNoteEntry(noteDTO,handlesError) ;
        }
        else
        if(noteDTO.getEditMode().equals(NoteBox.EditMode.EXTERNAL.name())){
            noteEntry = new ExternalNoteEntry(noteDTO,handlesError) ;
        }


        return noteEntry;
    }

}
