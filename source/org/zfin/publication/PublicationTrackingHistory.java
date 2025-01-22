package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;
import org.zfin.publication.presentation.PublicationEvent;

import jakarta.persistence.*;
import java.util.GregorianCalendar;

@Setter
@Getter
@Entity
@Table(name = "pub_tracking_history")
public class PublicationTrackingHistory implements PublicationEvent {

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

    public Boolean isCurrent() {
        return isCurrent;
    }

    @Override
    public Person getPerformedBy() {
        return updater;
    }

    @Override
    public String getDisplay() {
        StringBuilder display = new StringBuilder();
        display.append("Status changed to <b>").append(status.getName().toString()).append("</b>");
        if (owner != null) {
            display.append("<br>Owner changed to <b>")
                    .append(owner.getFirstName())
                    .append(" ")
                    .append(owner.getLastName())
                    .append("</b>");
        }
        if (location != null) {
            display.append("<br>Location changed to <b>")
                    .append(location.getName().toString())
                    .append("</b>");
        }
        return display.toString();
    }
}
