package org.zfin.ontology;

import javax.persistence.*;

/**
 * Basic implementation of the Term interface.
 */
@Entity
@Table(name = "obsolete_term_replacement")
public class ReplacementTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obstermrep_pk_id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "obstermrep_term_zdb_id")
    private GenericTerm obsoletedTerm;
    @ManyToOne
    @JoinColumn(name = "obstermrep_term_replacement_zdb_id")
    private GenericTerm replacementTerm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GenericTerm getObsoletedTerm() {
        return obsoletedTerm;
    }

    public void setObsoletedTerm(GenericTerm obsoletedTerm) {
        this.obsoletedTerm = obsoletedTerm;
    }

    public GenericTerm getReplacementTerm() {
        return replacementTerm;
    }

    public void setReplacementTerm(GenericTerm replacementTerm) {
        this.replacementTerm = replacementTerm;
    }
}
