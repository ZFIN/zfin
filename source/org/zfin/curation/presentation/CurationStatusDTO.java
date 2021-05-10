package org.zfin.curation.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;

import java.util.Calendar;

public class CurationStatusDTO {

    @JsonView(View.API.class) private Boolean current;
    @JsonView(View.API.class) private String pubZdbID;
    @JsonView(View.API.class) private PublicationTrackingStatus status;
    @JsonView(View.API.class) private PublicationTrackingLocation location;
    @JsonView(View.API.class) private PersonDTO owner;
    @JsonView(View.API.class) private Calendar updateDate;

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
