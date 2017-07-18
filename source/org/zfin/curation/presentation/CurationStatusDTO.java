package org.zfin.curation.presentation;

import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.util.Calendar;

public class CurationStatusDTO {

    private Boolean current;
    private String pubZdbID;
    private PublicationTrackingStatus status;
    private PublicationTrackingLocation location;
    private PersonDTO owner;
    private Calendar updateDate;

    public Boolean isCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
    }

    public String getPubZdbID() {
        return pubZdbID;
    }

    public void setPubZdbID(String pubZdbID) {
        this.pubZdbID = pubZdbID;
    }

    public PublicationTrackingStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationTrackingStatus status) {
        this.status = status;
    }

    public PublicationTrackingLocation getLocation() {
        return location;
    }

    public void setLocation(PublicationTrackingLocation location) {
        this.location = location;
    }

    public PersonDTO getOwner() {
        return owner;
    }

    public void setOwner(PersonDTO owner) {
        this.owner = owner;
    }

    public Calendar getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Calendar updateDate) {
        this.updateDate = updateDate;
    }
}
