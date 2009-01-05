package org.zfin.sequence.reno;

import org.zfin.publication.Publication;

/**
 * This class represents a Reno Redundancy run,
 * which is unique from a general run in that it has a relation publication.
 */
public class RedundancyRun extends Run{

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
