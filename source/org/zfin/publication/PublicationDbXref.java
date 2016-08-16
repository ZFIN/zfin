package org.zfin.publication;

import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;

@Entity
@Table(name = "pub_db_xref")
public class PublicationDbXref {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pdx_pk_id")
    private long Id;

    @ManyToOne
    @JoinColumn(name = "pdx_pub_zdb_id")
    private Publication publication;

    @Column(name = "pdx_accession_number")
    private String accessionNumber;
    
    @ManyToOne
    @JoinColumn(name = "pdx_fdbcont_zdb_id")
    private ReferenceDatabase referenceDatabase;

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

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }
}
