package org.zfin.infrastructure;

import org.zfin.ontology.GenericTerm;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.io.Serializable;


@Entity
@DiscriminatorValue("Term   ")
public class TermAttribution extends PublicationAttribution implements Serializable {

    @ManyToOne
    @JoinColumn(name = "recattrib_data_zdb_id", insertable = false, updatable = false)
    private GenericTerm term;

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
        setDataZdbID(term.getZdbID());
    }

}