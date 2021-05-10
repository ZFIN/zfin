package org.zfin.infrastructure;

import org.zfin.ontology.GenericTerm;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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