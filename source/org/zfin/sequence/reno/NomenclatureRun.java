package org.zfin.sequence.reno;

import org.zfin.publication.Publication;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;


/**
 * This class represents a Reno Nomenclature run,
 * which is unique from a general run in that it has an orthology publication.
 */
@Entity
@DiscriminatorValue("Nomenclature")
public class NomenclatureRun extends Run {

    @ManyToOne
    @JoinColumn(name = "run_relation_pub_zdb_id")
    private Publication orthologyPublication;

    public Publication getOrthologyPublication() {
        return orthologyPublication;
    }

    public void setOrthologyPublication(Publication orthologyPublication) {
        this.orthologyPublication = orthologyPublication;
    }

    public Type getType() {
        return Type.NOMENCLATURE;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n\t");
        sb.append("relation publication: ").append(orthologyPublication);
        return sb.toString();
    }
}
