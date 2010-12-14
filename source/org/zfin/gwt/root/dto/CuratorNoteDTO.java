package org.zfin.gwt.root.dto;

/**
 */
public class CuratorNoteDTO extends NoteDTO{

    public CuratorNoteDTO(){
        noteEditMode = NoteEditMode.PRIVATE ;
    }

    public CuratorNoteDTO(String zdbId,String dataZdbID,String note){
        super(zdbId,dataZdbID,NoteEditMode.PRIVATE,note) ;
    }

    public CuratorNoteDTO(String dataZdbID,String note){
        super(dataZdbID,NoteEditMode.PRIVATE,note) ;
    }

}
