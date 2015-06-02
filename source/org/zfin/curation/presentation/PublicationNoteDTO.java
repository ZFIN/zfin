package org.zfin.curation.presentation;

public class PublicationNoteDTO {

    private String zdbID;
    private String text;
    private String date;
    private CuratorDTO curator;

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CuratorDTO getCurator() {
        return curator;
    }

    public void setCurator(CuratorDTO curator) {
        this.curator = curator;
    }
}
