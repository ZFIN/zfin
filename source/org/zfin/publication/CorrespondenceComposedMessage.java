package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pub_correspondence_sent_email")
public class CorrespondenceComposedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcse_pk_id")
    private long id;

    @Column(name = "pubcse_date_composed")
    private Date composedDate;

    @ManyToOne
    @JoinColumn(name = "pubcse_from")
    private Person from;

    @Column(name = "pubcse_text")
    private String text;

    @Column(name = "pubcse_subject")
    private String subject;

    @ManyToOne
    @JoinColumn(name = "pubcse_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "pubcse_recipient_group_id")
    private CorrespondenceRecipientGroup recipientGroup;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getComposedDate() {
        return composedDate;
    }

    public void setComposedDate(Date composedDate) {
        this.composedDate = composedDate;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
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

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public CorrespondenceRecipientGroup getRecipientGroup() {
        return recipientGroup;
    }

    public void setRecipientGroup(CorrespondenceRecipientGroup recipientGroup) {
        this.recipientGroup = recipientGroup;
    }
}
