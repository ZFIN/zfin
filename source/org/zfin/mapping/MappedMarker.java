package org.zfin.mapping;

import org.zfin.people.Person;
import org.zfin.people.Lab;

public class MappedMarker {
    private String zdbID;
    private String refcrossId;
    private String markerId;
    private Person submitter;
    private Lab lab;
    private Person owner;
    private String comments;
    private String lg;
    private Float lgLocation;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getRefcrossId() {
        return refcrossId;
    }

    public void setRefcrossId(String refcrossId) {
        this.refcrossId = refcrossId;
    }

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    public Lab getLab() {
        return lab;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }

    public Float getLgLocation() {
        return lgLocation;
    }

    public void setLgLocation(Float lgLocation) {
        this.lgLocation = lgLocation;
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }
}
