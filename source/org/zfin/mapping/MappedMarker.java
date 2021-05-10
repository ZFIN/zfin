package org.zfin.mapping;

import org.zfin.infrastructure.ZdbID;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;

public abstract class MappedMarker implements Comparable, ZdbID {
    protected String zdbID;
    protected Panel panel;
    protected Person submitter;
    protected Lab lab;
    protected Person owner;
    protected String comments;
    protected String lg;
    protected String mappedName;
    protected String scoringData;
    protected String metric;
    protected Float lgLocation;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Panel getPanel() {
        return panel;
    }

    public void setPanel(Panel panel) {
        this.panel = panel;
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

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getScoringData() {
        return scoringData;
    }

    public void setScoringData(String scoringData) {
        this.scoringData = scoringData;
    }

    public abstract String getEntityID();

    public abstract String getEntityAbbreviation();
}
