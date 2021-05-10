package org.zfin.curation.presentation;

import java.util.Date;

public class PublicationNoteDTO {

    private String zdbID;
    private String text;
    private Date date;
    private PersonDTO curator;
    private boolean editable;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PersonDTO getCurator() {
        return curator;
    }

    public void setCurator(PersonDTO curator) {
        this.curator = curator;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
