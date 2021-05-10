package org.zfin.ontology;

import org.zfin.infrastructure.DataAlias;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Synonyms of term names.
 */
@Entity
@DiscriminatorValue("Term  ")
public class TermAlias extends DataAlias {

    @ManyToOne
    @JoinColumn(name = "dalias_data_zdb_id")
    private GenericTerm term;

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }
}
