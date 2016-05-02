package org.zfin.audit;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Date;

/**
 * This class defines a single item that was changed in a single attribute.
 */
@Entity
@Table(name = "UPDATES")
public class AuditLogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upd_pk_id")
    private long auditLogID;
    @Column(name = "rec_id")
    private String zdbID;
    @Column(name = "field_name")
    private String fieldName;
    @Column(name = "new_value")
    private String newValue;
    @Column(name = "old_value")
    private String oldValue;
    @Column(name = "comments")
    private String comment;
    @Column(name = "when")
    private Date dateUpdated;
    @ManyToOne()
    @JoinColumn(name = "submitter_id")
    private Person owner;

    public long getAuditLogID() {
        return auditLogID;
    }

    public void setAuditLogID(long auditLogID) {
        this.auditLogID = auditLogID;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }
}
