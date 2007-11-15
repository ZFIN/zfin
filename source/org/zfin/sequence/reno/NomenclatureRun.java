package org.zfin.sequence.reno;

import org.zfin.publication.Publication;


public class NomenclatureRun extends Run{
    protected  Publication orthologyPublication;

    public Publication getOrthologyPublication() {
        return orthologyPublication;
    }

    public void setOrthologyPublication(Publication orthologyPublication) {
        this.orthologyPublication = orthologyPublication;
    }


    public boolean isRedundancy() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isNomenclature() {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Type getType() {
        return Type.NOMENCLATURE;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append("relation publication: ").append(orthologyPublication);
        sb.append("\n\t");
        sb.append("instace of NomenclatureRun: ").append(this instanceof NomenclatureRun);
        sb.append("\n\t");
        return sb.toString();
    }
}
