package org.zfin.marker;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.infrastructure.PublicationAttribution;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "marker_history")
public class MarkerHistory implements Comparable<MarkerHistory>, EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "NOMEN"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "mhist_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "mhist_mrkr_zdb_id")
    private Marker marker;
    @Column(name = "mhist_reason", nullable = false)
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.marker.MarkerHistory$Reason")})
    private Reason reason;
    @Transient
    private String event;
    @Column(name = "mhist_event", nullable = false)
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.marker.MarkerHistory$Event")})
    private Event eventType;
    //name after renaming event
    @Column(name = "mhist_mrkr_name_on_mhist_date")
    private String name;
    //abbreviation after renaming event
    @Column(name = "mhist_mrkr_abbrev_on_mhist_date")
    private String symbol;
    @Column(name = "mhist_date")
    private Date date;
    @ManyToOne
    @JoinColumn(name = "mhist_dalias_zdb_id")
    private MarkerAlias markerAlias;
    @Column(name = "mhist_mrkr_prev_name", nullable = false)
    private String oldMarkerName;
    @Column(name = "mhist_comments")
    private String comments;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recattrib_data_zdb_id")
    private Set<PublicationAttribution> attributions;


    public Reason[] getReasonArray() {
        return Reason.values();
    }

    @Override
    public int compareTo(MarkerHistory o) {
        return -date.compareTo(o.getDate());
    }

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

        public static Reason getReason(String reasonValue) {
            for (Reason reason : values()) {
                if (reason.value.equals(reasonValue))
                    return reason;
            }
            return null;
        }

        public String toString() {
            return value;
        }
    }

    public enum Event {
        // original
        ASSIGNED("assigned", "assigned"),
        REASSIGNED("reassigned", "renamed from"),
        RENAMED("renamed", "renamed from"),
        // previously unmapped from marker_history_event
        MERGED("merged", "merged with"),
        RESERVED("reserved", "reserved");

        private String value;
        private String display;

        private Event(String value, String display) {
            this.value = value;
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public String toString() {
            return this.value;
        }

    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Event getEventType() {
        return eventType;
    }

    public void setEventType(Event eventType) {
        this.eventType = eventType;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
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

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getAbbreviation() {
        return eventType.getDisplay() + ": " + getOldSymbol() + " to " + symbol;
    }

    @Override
    public String getAbbreviationOrder() {
        return event;
    }

    @Override
    public String getEntityType() {
        return "Marker Event (Marker History)";
    }

    @Override
    public String getEntityName() {
        return "Marker Event (Marker History)";
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<PublicationAttribution> getAttributions() {
        return attributions;
    }

    public void setAttributions(Set<PublicationAttribution> attributions) {
        this.attributions = attributions;
    }

    public MarkerAlias getMarkerAlias() {
        return markerAlias;
    }

    public void setMarkerAlias(MarkerAlias markerAlias) {
        this.markerAlias = markerAlias;
    }


    public String getOldMarkerName() {
        return oldMarkerName;
    }

    public void setOldMarkerName(String oldMarkerName) {
        this.oldMarkerName = oldMarkerName;
    }

    public String getOldSymbol() {
        switch (eventType) {
            case REASSIGNED:
                return markerAlias.getAlias();
            case MERGED:
                return markerAlias.getAlias();
            case RENAMED:
                return oldMarkerName;
        }
        return "";
    }
}
