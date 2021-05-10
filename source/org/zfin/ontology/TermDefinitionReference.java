package org.zfin.ontology;

import org.zfin.sequence.ForeignDB;

import javax.persistence.*;

/**
 * Term definition reference.
 */
@Entity
@Table(name = "external_reference")
public class TermDefinitionReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exref_pk_id")
    private long ID;
    @ManyToOne
    @JoinColumn(name = "exref_data_zdb_id")
    private GenericTerm term;
    @ManyToOne
    @JoinColumn(name = "exref_foreign_db_id")
    private ForeignDB foreignDB;
    @Column(name = "exref_reference")
    private String reference;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public ForeignDB getForeignDB() {
        return foreignDB;
    }

    public void setForeignDB(ForeignDB foreignDB) {
        this.foreignDB = foreignDB;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }
}
