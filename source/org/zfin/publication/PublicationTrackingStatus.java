package org.zfin.publication;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

import jakarta.persistence.*;
import org.zfin.framework.entity.BaseEntity;

import java.util.Arrays;

@Setter
@Getter
@Entity
@Table(name = "pub_tracking_status")
public class PublicationTrackingStatus extends BaseEntity {

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
    @JsonView(View.API.class)
    private long id;

    @Column(name = "pts_status")
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class, parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingStatus$Type")})
    @JsonView(View.API.class)
    private Type type;

    @Column(name = "pts_status_display")
    @org.hibernate.annotations.Type(value = org.zfin.framework.StringEnumValueUserType.class, parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingStatus$Name")})
    @JsonView(View.API.class)
    private Name name;

    @Column(name = "pts_status_qualifier")
    @JsonView(View.API.class)
    private String qualifier;

    @Column(name = "pts_terminal_status")
    @JsonView(View.API.class)
    private boolean isTerminal;

    @Column(name = "pts_hidden_status")
    @JsonView(View.API.class)
    private boolean hidden;

    @Column(name = "pts_dashboard_order")
    @JsonView(View.API.class)
    private int dashboardOrder;

    public boolean isTerminal() {
        return isTerminal;
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
