package org.zfin.zebrashare;


import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import javax.persistence.*;

@Entity
@Table(name="zebrashare_data_edit_permission")
public class ZebrashareEditor {

    @Id
    @Column(name = "zdep_zdb_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zdbID")
    @GenericGenerator(name = "zdbID", strategy = "org.zfin.database.ZdbIdGenerator", parameters = {@Parameter(name = "type", value = "ZDEP")})
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "zdep_pub_zdb_id")
    private Publication publication;

    @ManyToOne
    @JoinColumn(name = "zdep_person_zdb_id")
    private Person person;

    @Column(name = "zdep_person_is_submitter")
    private boolean submitter;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public boolean isSubmitter() {
        return submitter;
    }

    public void setSubmitter(boolean submitter) {
        this.submitter = submitter;
    }
}
