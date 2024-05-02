package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import jakarta.persistence.*;
import java.util.Date;

@Setter
@Getter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pubcst_pub_zdb_id")
    private Publication publication;

    @Column(name = "pubcst_resend")
    private boolean resend;

}
