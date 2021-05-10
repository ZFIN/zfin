package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pub_correspondence_received_email")
public class CorrespondenceReceivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcre_pk_id")
    private long id;

    @Column(name = "pubcre_correspondence_from_first_name")
    private String fromFirstName;

    @Column(name = "pubcre_correspondence_from_last_name")
    private String fromLastName;

    @Column(name = "pubcre_correspondence_from_email_address")
    private String fromEmail;

    @ManyToOne
    @JoinColumn(name = "pubcre_correspondence_from_person_zdb_id")
    private Person from;

    @ManyToOne
    @JoinColumn(name = "pubcre_pub_zdb_id")
    private Publication publication;

    @Column(name = "pubcre_received_date")
    private Date date;

    @Column(name = "pubcre_text")
    private String text;

    @Column(name = "pubcre_subject")
    private String subject;

    @ManyToOne
    @JoinColumn(name = "pubcre_received_by")
    private Person to;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFromFirstName() {
        return fromFirstName;
    }

    public void setFromFirstName(String fromFirstName) {
        this.fromFirstName = fromFirstName;
    }

    public String getFromLastName() {
        return fromLastName;
    }

    public void setFromLastName(String fromLastName) {
        this.fromLastName = fromLastName;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Person getTo() {
        return to;
    }

    public void setTo(Person to) {
        this.to = to;
    }
}
