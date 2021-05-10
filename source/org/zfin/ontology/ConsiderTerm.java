package org.zfin.ontology;

import javax.persistence.*;

/**
 * Basic implementation of the Term interface.
 */
@Entity
@Table(name = "obsolete_term_suggestion")
public class ConsiderTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obstermsug_pk_id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "obstermsug_term_zdb_id")
    private GenericTerm obsoletedTerm;
    @ManyToOne
    @JoinColumn(name = "obstermsug_term_suggestion_zdb_id")
    private GenericTerm considerTerm;

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

    public GenericTerm getConsiderTerm() {
        return considerTerm;
    }

    public void setConsiderTerm(GenericTerm considerTerm) {
        this.considerTerm = considerTerm;
    }
}
