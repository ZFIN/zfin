package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.server.DTOConversionService;

/**
 * For use with the Note objects.
 */
public class NoteDTO implements IsSerializable {

    protected String zdbID ;
    protected String dataZdbID ;
    protected String noteData ;
    protected String publicationZdbID;
    protected String noteType;
    protected String noteTag;
    protected PublicationDTO publicationDTO;
    protected NoteEditMode noteEditMode;

    public NoteDTO(){}

    public NoteDTO(String zdbId, String dataZdbID,NoteEditMode noteEditMode,String note, String noteType, String noteTag){
        this.zdbID = zdbId ;
        this.dataZdbID = dataZdbID ;
        this.noteEditMode = noteEditMode;
        this.noteData = note;
        this.noteType = noteType;
        this.noteTag = noteTag;
    }

    public NoteDTO(String zdbId, String dataZdbID,NoteEditMode noteEditMode,String note){
        this.zdbID = zdbId ;
        this.dataZdbID = dataZdbID ;
        this.noteEditMode = noteEditMode;
        this.noteData = note;

    }

    public NoteDTO(String dataZdbID,NoteEditMode noteEditMode,String note){
        this.dataZdbID = dataZdbID ;
        this.noteEditMode = noteEditMode;
        this.noteData = note;
    }


    public NoteDTO(String dataZdbID){
        this.dataZdbID = dataZdbID ;
    }

    public NoteDTO clone(){
        NoteDTO noteDTO = new NoteDTO();
        noteDTO.setZdbID(zdbID);
        noteDTO.setDataZdbID(dataZdbID);
        noteDTO.setNoteData(noteData);
        noteDTO.setPublicationZdbID(publicationZdbID);
        noteDTO.setNoteEditMode(noteEditMode);
        noteDTO.setNoteType(noteType);
        noteDTO.setNoteTag(noteTag);
        return noteDTO;
    }
    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
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

    public PublicationDTO getPublicationDTO() {
        return publicationDTO;
    }

    public void setPublicationDTO(PublicationDTO publicationDTO) {
        this.publicationDTO = publicationDTO;
    }

    public NoteEditMode getNoteEditMode() {
        return noteEditMode;
    }

    public void setNoteEditMode(NoteEditMode noteEditMode) {
        this.noteEditMode = noteEditMode;
    }

    public boolean isEmpty(){
        return noteData==null || noteData.trim().length()==0 ;
    }

    public String getNoteTag() {
        return noteTag;
    }

    public void setNoteTag(String noteTag) {
        this.noteTag = noteTag;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NoteDTO");
        sb.append("{dataZdbID='").append(dataZdbID).append('\'');
        sb.append(", zdbID='").append(zdbID).append('\'');
        sb.append(", noteData='").append(noteData).append('\'');
        sb.append(", editMode='").append(noteEditMode).append('\'');
        sb.append(", publicationZdbID='").append(publicationZdbID).append('\'');
        sb.append(", noteType='").append(noteType).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
