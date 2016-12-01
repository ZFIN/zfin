package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pub_correspondence_sent_tracker")
public class CorrespondenceSentMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcst_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "pubcst_sent_by")
    private Person from;

    @Column(name = "pubcst_date_sent")
    private Date sentDate;

    @ManyToOne
    @JoinColumn(name = "pubcst_sent_email_id")
    private CorrespondenceComposedMessage message;

    @ManyToOne
    @JoinColumn(name = "pubcst_pub_zdb_id")
    private Publication publication;

    @Column(name = "pubcst_resend")
    private boolean resend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Person getFrom() {
        return from;
    }

    public void setFrom(Person from) {
        this.from = from;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public CorrespondenceComposedMessage getMessage() {
        return message;
    }

    public void setMessage(CorrespondenceComposedMessage message) {
        this.message = message;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public boolean isResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        this.resend = resend;
    }
}
