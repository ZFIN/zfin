package org.zfin.publication;

import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;

@Entity
@Table(name = "pubmed_publication_author")
public class PubmedPublicationAuthor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ppa_pk_id")
    private long Id;

    @ManyToOne
    @JoinColumn(name = "ppa_publication_zdb_id")
    private Publication publication;

    @Column(name = "ppa_pubmed_id")
    private String pubmedId;

    @Column(name = "ppa_author_last_name")
    private String lastName;

    @Column(name = "ppa_author_first_name")
    private String firstName;

    @Column(name = "ppa_author_middle_name")
    private String middleName;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
}


