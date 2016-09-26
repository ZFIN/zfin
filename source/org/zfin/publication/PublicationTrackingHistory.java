package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;
import java.util.GregorianCalendar;

@Entity
@Table(name = "pub_tracking_history")
public class PublicationTrackingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pth_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "pth_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "pth_status_id")
    private PublicationTrackingStatus status;

    @ManyToOne
    @JoinColumn(name = "pth_location_id")
    private PublicationTrackingLocation location;

    @ManyToOne
    @JoinColumn(name = "pth_claimed_by")
    private Person owner;

    @ManyToOne
    @JoinColumn(name = "pth_status_set_by")
    private Person updater;

    @Column(name = "pth_status_insert_date")
    private GregorianCalendar date;

    @Column(name = "pth_status_is_current", updatable = false, insertable = false)
    private Boolean isCurrent;

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

    public PublicationTrackingStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationTrackingStatus status) {
        this.status = status;
    }

    public PublicationTrackingLocation getLocation() {
        return location;
    }

    public void setLocation(PublicationTrackingLocation location) {
        this.location = location;
    }

    public Person getOwner() {
        return owner;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public Person getUpdater() {
        return updater;
    }

    public void setUpdater(Person updater) {
        this.updater = updater;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public Boolean isCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }
}
