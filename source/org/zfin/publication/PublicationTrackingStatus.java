package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.*;
import java.util.Arrays;

@Entity
@Table(name = "pub_tracking_status")
public class PublicationTrackingStatus {

    public enum Type {
        NEW,
        READY_FOR_PROCESSING,
        PROCESSING,
        MANUAL_PDF,
        READY_FOR_INDEXING,
        INDEXING,
        READY_FOR_CURATION,
        CURATING,
        WAIT,
        CLOSED
    }

    public enum Name {
        NEW("New"),
        READY_FOR_PROCESSING("Ready for Processing"),
        PROCESSING("Processing"),
        MANUAL_PDF("Manual PDF Acquisition Needed"),
        READY_FOR_INDEXING("Ready for Indexing"),
        INDEXING("Indexing"),
        READY_FOR_CURATION("Ready for Curation"),
        CURATING("Curating"),
        WAITING_FOR_CURATOR_REVIEW("Waiting for Curator Review"),
        WAITING_FOR_SOFTWARE_FIX("Waiting for Software Fix"),
        WAITING_FOR_AUTHOR("Waiting for Author"),
        WAITING_FOR_ORTHOLOGY("Waiting for Ontology"),
        WAITING_FOR_NOMENCLATURE("Waiting for Nomenclature"),
        WAITING_FOR_ACTIVATION("Waiting for Activation"),
        WAITING_FOR_PDF("Waiting for PDF"),
        CLOSED_CURATED("Closed, Curated"),
        CLOSED_ARCHIVED("Closed, Archived"),
        CLOSED_NO_DATA("Closed, No data"),
        CLOSED_NOT_ZEBRAFISH("Closed, Not a zebrafish paper"),
        CLOSED_NO_PDF("Closed, No PDF"),
        CLOSED_PARTIALLY_CURATED("Closed, Partially curated");

        private String display;

        Name(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        @Override
        @JsonValue
        public String toString() {
            return display;
        }

        public static Name fromString(String value) throws Exception {
            return Arrays.stream(values()).filter(n -> n.display.equals(value)).findAny().orElse(null);
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pts_pk_id")
    private long id;

    @Column(name = "pts_status")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingStatus$Type")})
    private Type type;

    @Column(name = "pts_status_display")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingStatus$Name")})
    private Name name;

    @Column(name = "pts_status_qualifier")
    private String qualifier;

    @Column(name = "pts_terminal_status")
    private boolean isTerminal;

    @Column(name = "pts_hidden_status")
    private boolean hidden;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(boolean isTerminal) {
        this.isTerminal = isTerminal;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return "PublicationTrackingStatus{" +
                "id=" + id +
                ", type=" + type +
                ", name=" + name +
                ", qualifier='" + qualifier + '\'' +
                ", isTerminal=" + isTerminal +
                ", hidden=" + hidden +
                '}';
    }
}
