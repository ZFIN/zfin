package org.zfin.curation.presentation;

import java.util.Date;
import java.util.List;

public class CorrespondenceDTO {

    private long id;
    private String pub;
    private boolean outgoing;
    private Date sentDate;
    private Date composedDate;
    private PersonDTO from;
    private List<PersonDTO> to;
    private String subject;
    private String message;
    private boolean resend;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Date getComposedDate() {
        return composedDate;
    }

    public void setComposedDate(Date composedDate) {
        this.composedDate = composedDate;
    }

    public PersonDTO getFrom() {
        return from;
    }

    public void setFrom(PersonDTO from) {
        this.from = from;
    }

    public List<PersonDTO> getTo() {
        return to;
    }

    public void setTo(List<PersonDTO> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResend() {
        return resend;
    }

    public void setResend(boolean resend) {
        this.resend = resend;
    }
}
