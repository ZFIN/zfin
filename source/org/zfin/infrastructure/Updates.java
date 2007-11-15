package org.zfin.infrastructure;

import org.zfin.people.Person;

import java.util.Date;

public class Updates {
    private Long ID;
    private String recID;
    private String submitterID;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String comments;
    private Date whenUpdated;
    private String submitterName;


    public Long getID() {
        return ID;
    }

    public void setID(Long ID) {
        this.ID = ID;
    }

    public String getRecID() {
        return recID;
    }

    public void setRecID(String recID) {
        this.recID = recID;
    }



    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }


    public String getSubmitterID() {
        return submitterID;
    }

    public void setSubmitterID(String submitterID) {
        this.submitterID = submitterID;
    }

    public Date getWhenUpdated() {
        return whenUpdated;
    }

    public void setWhenUpdated(Date whenUpdated) {
        this.whenUpdated = whenUpdated;
    }
     public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }
}
