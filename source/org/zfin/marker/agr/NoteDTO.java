package org.zfin.marker.agr;

import java.util.List;
public class NoteDTO {

   // private List<String> note;
    private String note;
    private PublicationAgrDTO references;

    public NoteDTO(PublicationAgrDTO references) {
        this.references = references;
    }


    /*public List<String> getNote() {
        return note;
    }

    public void setNote(List<String> note) {
        this.note = note;
    }*/

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PublicationAgrDTO getReferences() {
        return references;

    }

}
