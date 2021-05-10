package org.zfin.gwt.root.dto;

/**
 * The external note has a pub associated with it.
 */
public class ExternalNoteDTO extends NoteDTO{

    public ExternalNoteDTO(){
        noteEditMode = NoteEditMode.EXTERNAL;
    }

    // right now only external notes will have a pub
    public ExternalNoteDTO(String dataZdbID,String note,String publicationZdbID){
        super(dataZdbID, NoteEditMode.EXTERNAL,note);
        this.publicationZdbID = publicationZdbID ;
    }

}
