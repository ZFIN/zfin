package org.zfin.gwt.root.dto;

/** The public note.
 *
 * The zdbId and the dataZdbId should both be that of the marker.
 */
public class PublicNoteDTO extends NoteDTO{

    public PublicNoteDTO(){
        noteEditMode = NoteEditMode.PUBLIC;
    }

    public PublicNoteDTO(String dataZdbID,String note){
        super(dataZdbID,NoteEditMode.PUBLIC,note) ;
        zdbID = dataZdbID ;
    }

}
