package org.zfin.marker.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * For use with the Note objects.
 */
public class NoteDTO implements IsSerializable {

    private String dataZdbID ;
    private String noteData ;
    private String indexNote ;
    private String curatorZdbID ;

    public NoteDTO(){}

    public NoteDTO(String dataZdbID){
        this.dataZdbID = dataZdbID ;
    }

    public NoteDTO(String dataZdbID,String noteData,String indexNote, String curatorZdbID){
        this.dataZdbID = dataZdbID ;
        this.noteData = noteData ;
        this.indexNote = indexNote ;
        this.curatorZdbID = curatorZdbID ;
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

    public String getCuratorZdbID() {
        return curatorZdbID;
    }

    public void setCuratorZdbID(String curatorZdbID) {
        this.curatorZdbID = curatorZdbID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("NoteDTO");
        sb.append("{dataZdbID='").append(dataZdbID).append('\'');
        sb.append(", noteData='").append(noteData).append('\'');
        sb.append(", indexNote='").append(indexNote).append('\'');
        sb.append(", curatorZdbID='").append(curatorZdbID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
