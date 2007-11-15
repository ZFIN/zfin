package org.zfin.sequence.reno;

import org.zfin.publication.Publication;

/**
 * User: nathandunn
 * Date: Oct 24, 2007
 * Time: 3:10:30 PM
 */
public class RedundancyRun extends Run{

    protected Publication relationPublication;


    public Publication getRelationPublication() {
        return relationPublication;
    }

    public void setRelationPublication(Publication relationPublication) {
        this.relationPublication = relationPublication;
    }


    public boolean isRedundancy() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNomenclature() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }



    public Type getType() {
        return Type.REDUNDANCY ;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("relation publication: ").append(relationPublication);
        sb.append("\n\t");
        sb.append("instace of RedundancyRun: ").append(this instanceof RedundancyRun);
        sb.append("\n\t");
        return sb.toString();
    }
}
