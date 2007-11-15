package org.zfin.infrastructure;

import org.zfin.people.Person;

import java.util.Date;

public class DataNote {
    private String zdbID;
    private String dataZdbID;
    private Person curator;
    private String note;
    private Date date;

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

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

