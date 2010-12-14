package org.zfin.gwt.curation.ui;

import org.zfin.gwt.root.dto.NoteDTO;
import org.zfin.gwt.root.ui.AbstractNoteEntry;
import org.zfin.gwt.root.ui.HandlesError;

/**
 * A factory class for creating notes by type.
 */
public class FeatureNoteEntryFactory {


    public static AbstractNoteEntry createFeatureNoteEntry(NoteDTO noteDTO, HandlesError handlesError) {

        switch (noteDTO.getNoteEditMode()){
            case PUBLIC:
                return new FeaturePublicNoteEntry(noteDTO, handlesError);
            case PRIVATE:
                return new FeaturePrivateNoteEntry(noteDTO, handlesError);
            
            default:
                return null ;
        }
    }

}
