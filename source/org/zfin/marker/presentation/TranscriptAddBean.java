package org.zfin.marker.presentation;

import java.util.Map;

/**
 */
public class TranscriptAddBean {
    private String name ;
    private String chosenType;
    private String ownerZdbID ;
    private String chosenStatus;
    private Map<String,String> transcriptTypeList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChosenType() {
        return chosenType;
    }

    public void setChosenType(String chosenType) {
        this.chosenType = chosenType;
    }

    public Map<String, String> getTranscriptTypeList() {
        return transcriptTypeList;
    }

    public void setTranscriptTypeList(Map<String, String> transcriptTypeList) {
        this.transcriptTypeList = transcriptTypeList;
    }

    public String getOwnerZdbID() {
        return ownerZdbID;
    }

    public void setOwnerZdbID(String ownerZdbID) {
        this.ownerZdbID = ownerZdbID;
    }

    public String getChosenStatus() {
        return chosenStatus;
    }

    public void setChosenStatus(String chosenStatus) {
        this.chosenStatus = chosenStatus;
    }
}
