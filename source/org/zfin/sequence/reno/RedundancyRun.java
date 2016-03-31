package org.zfin.sequence.reno;

import org.zfin.publication.Publication;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * This class represents a Reno Redundancy run,
 * which is unique from a general run in that it has a relation publication.
 */
@Entity
@DiscriminatorValue("Redundancy")
public class RedundancyRun extends Run {

    @ManyToOne
    @JoinColumn(name = "run_relation_pub_zdb_id")
    private Publication relationPublication;


    public Publication getRelationPublication() {
        return relationPublication;
    }

    public void setRelationPublication(Publication relationPublication) {
        this.relationPublication = relationPublication;
    }

    public Type getType() {
        return Type.REDUNDANCY ;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("\n\t");
        sb.append("relation publication: ").append(relationPublication);
        return sb.toString();
    }
}
