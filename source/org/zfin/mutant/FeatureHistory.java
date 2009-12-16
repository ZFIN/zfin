package org.zfin.mutant;


import java.util.Date;

public class FeatureHistory {

    public enum Reason {
        NOT_SPECIFIED("Not Specified"),
        PER_GENE_FAMILIY_REVISION("per gene family revision"),
        PER_PERSONAL_COMMUNICATION_WITH_AUTHORS("per personal communication with authors"),
        RENAMED_TO_CONFORM_WITH_HUMAN_NOMENCLATURE("renamed to conform with human nomenclature"),
        RENAMED_TO_CONFORM_WITH_MOUSE_NOMENCLATURE("renamed to conform with mouse nomenclature"),
        RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES("renamed to conform with zebrafish guidelines"),
        SAME_MARKER("same marker");
//        RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE("renamed through the nomenclature pipeline");

        private String value;

        Reason(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }
    }

    public enum Event {
        // original
        ASSIGNED("assigned"),
        REASSIGNED("reassigned"),
        RENAMED("renamed"),


        // previously unmnapped from marker_history_event
        MERGED("merged"),
        RESERVED("reserved"),;

        private String value;

        private Event(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }

    }

    private String zdbID;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public FeatureAlias getFeatureAlias() {
        return featureAlias;
    }

    public void setFeatureAlias(FeatureAlias featureAlias) {
        this.featureAlias = featureAlias;
    }

    private Feature feature;
    private Reason reason;
    private String event;
    //name after renaming event
    private String name;
    //abbreviation after renaming event
    private String abbreviation;
    private Date date;
    private FeatureAlias featureAlias;
    private String oldMarkerName;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason reason) {
        this.reason = reason;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public String getOldMarkerName() {
        return oldMarkerName;
    }

    public void setOldMarkerName(String oldMarkerName) {
        this.oldMarkerName = oldMarkerName;
    }
}
