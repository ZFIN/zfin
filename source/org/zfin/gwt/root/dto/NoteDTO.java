package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For use with the Note objects.
 */
public class NoteDTO implements IsSerializable {

    private String zdbID ;
    private String dataZdbID ;
    private String noteData ;
    private String publicationZdbID;
    private String editMode;

    public NoteDTO(){}

    public NoteDTO(String dataZdbID,String editMode,String note){
        this.dataZdbID = dataZdbID ;
        this.editMode = editMode;
        this.noteData = note;
    }

    public NoteDTO(String dataZdbID,String editMode,String note,String publicationZdbID){
        this(dataZdbID,editMode,note);
        this.publicationZdbID = publicationZdbID ;
    }

    public NoteDTO(String dataZdbID){
        this.dataZdbID = dataZdbID ;
    }

    public NoteDTO clone(){
        NoteDTO noteDTO = new NoteDTO(dataZdbID,editMode,noteData,publicationZdbID);
        noteDTO.setZdbID(zdbID);
        return noteDTO;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getNoteData() {
        return noteData;
    }

    public void setNoteData(String noteData) {
        this.noteData = noteData;
    }

    public String getPublicationZdbID() {
        return publicationZdbID;
    }

    public void setPublicationZdbID(String publicationZdbID) {
        this.publicationZdbID = publicationZdbID;
    }

    public String getEditMode() {
        return editMode;
    }

    public void setEditMode(String editMode) {
        this.editMode = editMode;
    }

    public boolean isEmpty(){
        return noteData==null || noteData.trim().length()==0 ;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NoteDTO");
        sb.append("{dataZdbID='").append(dataZdbID).append('\'');
        sb.append(", zdbID='").append(zdbID).append('\'');
        sb.append(", noteData='").append(noteData).append('\'');
        sb.append(", editMode='").append(editMode).append('\'');
        sb.append(", publicationZdbID='").append(publicationZdbID).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
