package org.zfin.publication;

import org.zfin.profile.Person;

import javax.persistence.*;

@Entity
@Table(name = "pub_correspondence_recipient")
public class CorrespondenceRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pubcr_pk_id")
    private long id;

    @Column(name = "pubcr_recipient_first_name")
    private String firstName;

    @Column(name = "pubcr_recipient_last_name")
    private String lastName;

    @Column(name = "pubcr_recipient_email_address")
    private String email;

    @ManyToOne
    @JoinColumn(name = "pubcr_recipient_person_zdb_id")
    private Person person;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
