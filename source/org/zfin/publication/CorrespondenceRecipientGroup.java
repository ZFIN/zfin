package org.zfin.publication;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "pub_correspondence_recipient_group")
public class CorrespondenceRecipientGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcrg_group_id")
    private long id;

    @OneToMany
    @JoinColumn(name = "pubcr_recipient_group_id")
    private Set<CorrespondenceRecipient> recipients;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<CorrespondenceRecipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<CorrespondenceRecipient> recipients) {
        this.recipients = recipients;
    }
}
