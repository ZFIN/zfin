package org.zfin.curation;

import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;

public class Correspondence {

    private long id;
    private Publication publication;
    private Person curator;
    private Date contactedDate;
    private Date respondedDate;
    private Date giveUpDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Person getCurator() {
        return curator;
    }

    public void setCurator(Person curator) {
        this.curator = curator;
    }

    public Date getContactedDate() {
        return contactedDate;
    }

    public void setContactedDate(Date contactedDate) {
        this.contactedDate = contactedDate;
    }

    public Date getRespondedDate() {
        return respondedDate;
    }

    public void setRespondedDate(Date respondedDate) {
        this.respondedDate = respondedDate;
    }

    public Date getGiveUpDate() {
        return giveUpDate;
    }

    public void setGiveUpDate(Date giveUpDate) {
        this.giveUpDate = giveUpDate;
    }
}
