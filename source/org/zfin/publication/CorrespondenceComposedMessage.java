package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

@Setter
@Getter
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
    @JoinColumn(name = "pubcse_sent_by")
    private Person from;

    @Column(name = "pubcse_text")
    private String text;

    @Column(name = "pubcse_subject")
    private String subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pubcse_pub_zdb_id")
    private Publication publication;

    @Column(name = "pubcse_recipient_group")
    private String recipientEmailList;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "message")
    private Set<CorrespondenceRecipient> recipients;

}
