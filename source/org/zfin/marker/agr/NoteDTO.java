package org.zfin.marker.agr;

import java.util.List;
public class NoteDTO {


    private String note;

    private List<PublicationAgrDTO> references;

    public List<PublicationAgrDTO> getReferences() {
        return references;
    }

    public NoteDTO(List<PublicationAgrDTO> references) {
        this.references = references;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }


}
