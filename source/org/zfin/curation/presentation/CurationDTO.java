package org.zfin.curation.presentation;

import java.util.Date;

public class CurationDTO {

    private String zdbID;
    private String topic;
    private PersonDTO curator;
    private boolean dataFound;
    private Date entryDate;
    private Date openedDate;
    private Date closedDate;


    public PersonDTO getCurator() {
        return curator;
    }

    public void setCurator(PersonDTO curator) {
        this.curator = curator;
    }

    public boolean isDataFound() {
        return dataFound;
    }

    public void setDataFound(boolean dataFound) {
        this.dataFound = dataFound;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public Date getOpenedDate() {
        return openedDate;
    }

    public void setOpenedDate(Date openedDate) {
        this.openedDate = openedDate;
    }

    public Date getClosedDate() {
        return closedDate;
    }

    public void setClosedDate(Date closedDate) {
        this.closedDate = closedDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

}
