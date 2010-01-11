package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For use with the Note objects.
 */
public class NoteDTO implements IsSerializable {

    private String dataZdbID ;
    private String noteData ;
    private String indexNote ;

    public NoteDTO(){}

    public NoteDTO(String dataZdbID){
        this.dataZdbID = dataZdbID ;
    }

    public NoteDTO(String dataZdbID,String noteData,String indexNote, String curatorZdbID){
        this.dataZdbID = dataZdbID ;
        this.noteData = noteData ;
        this.indexNote = indexNote ;
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

    public String getIndexNote() {
        return indexNote;
    }

    public void setIndexNote(String indexNote) {
        this.indexNote = indexNote;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NoteDTO");
        sb.append("{dataZdbID='").append(dataZdbID).append('\'');
        sb.append(", noteData='").append(noteData).append('\'');
        sb.append(", indexNote='").append(indexNote).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
