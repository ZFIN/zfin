package org.zfin.curation;

import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "publication_correspondence")
public class Correspondence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcorr_pk_id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "pubcorr_pub_zdb_id")
    private Publication publication;
    @ManyToOne
    @JoinColumn(name = "pubcorr_curator_zdb_id")
    private Person curator;
    @Column(name = "pubcorr_contacted_date")
    private Date contactedDate;
    @Column(name = "pubcorr_responded_date")
    private Date respondedDate;
    @Column(name = "pubcorr_gave_up_date")
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
