package org.zfin.infrastructure;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "updates")
public class Updates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upd_pk_id")
    private Long ID;
    @Column(name = "rec_id")
    private String recID;
    @ManyToOne
    @JoinColumn(name = "submitter_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Person submitter;
    @Column(name = "field_name")
    private String fieldName;
    @Column(name = "old_value")
    private String oldValue;
    @Column(name = "new_value")
    private String newValue;
    @Column(name = "comments")
    private String comments;
    @Column(name = "upd_when")
    private Date whenUpdated;
    @Column(name = "submitter_name")
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

    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
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
