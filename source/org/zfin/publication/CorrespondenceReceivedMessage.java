package org.zfin.publication;

import lombok.Getter;
import lombok.Setter;
import org.zfin.profile.Person;

import jakarta.persistence.*;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "pub_correspondence_received_email")
public class CorrespondenceReceivedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcre_pk_id")
    private long id;

    @Column(name = "pubcre_correspondence_from_first_name")
    private String fromFirstName;

    @Column(name = "pubcre_correspondence_from_last_name")
    private String fromLastName;

    @Column(name = "pubcre_correspondence_from_email_address")
    private String fromEmail;

    @ManyToOne
    @JoinColumn(name = "pubcre_correspondence_from_person_zdb_id")
    private Person from;

    @ManyToOne
    @JoinColumn(name = "pubcre_pub_zdb_id")
    private Publication publication;

    @Column(name = "pubcre_received_date")
    private Date date;

    @Column(name = "pubcre_text")
    private String text;

    @Column(name = "pubcre_subject")
    private String subject;

    @ManyToOne
    @JoinColumn(name = "pubcre_received_by")
    private Person to;

}
