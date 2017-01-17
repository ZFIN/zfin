package org.zfin.publication;

import javax.persistence.*;

@Entity
@Table(name = "pub_tracking_status")
public class PublicationTrackingStatus {

    public enum Type {
        NEW,
        READY_FOR_INDEXING,
        INDEXING,
        INDEXED,
        READY_FOR_CURATION,
        CURATING,
        WAIT,
        CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pts_pk_id")
    private long id;

    @Column(name = "pts_status")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType", parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value="org.zfin.publication.PublicationTrackingStatus$Type")})
    private Type type;

    @Column(name = "pts_status_display")
    private String name;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
}
