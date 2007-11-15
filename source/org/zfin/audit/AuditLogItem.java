package org.zfin.audit;

import org.zfin.people.User;

import java.util.Date;

/**
 * This class defines a single item that was changed in a single attribute.
 */
public class AuditLogItem {

    private long auditLogID;
    private String zdbID;
    private String fieldName;
    private String newValue;
    private String oldValue;
    private String comment;
    private Date dateUpdated;
    private User owner;

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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }
}
