package org.zfin.gwt.root.dto;

import java.util.Date;

/**
 */
public class CuratorNoteDTO extends NoteDTO {

    private PersonDTO curator;
    private Date date;

    public CuratorNoteDTO() {
        noteEditMode = NoteEditMode.PRIVATE;
    }

    public CuratorNoteDTO(String zdbId, String dataZdbID, String note) {
        super(zdbId, dataZdbID, NoteEditMode.PRIVATE, note);
    }

    public CuratorNoteDTO(String dataZdbID, String note) {
        super(dataZdbID, NoteEditMode.PRIVATE, note);
    }

    public PersonDTO getCurator() {
        return curator;
    }

    public void setCurator(PersonDTO curator) {
        this.curator = curator;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
