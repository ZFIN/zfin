package org.zfin.sequence;

import org.zfin.Species;

public class EntrezProtRelation implements Comparable<EntrezProtRelation> {

    private long epID;
    private Species.Type organism;
    private String proteinAccNum;
    private Entrez entrezAccession;

    public long getEpID() {
        return epID;
    }

    public void setEpID(long epID) {
        this.epID = epID;
    }


    public Species.Type getOrganism() {
        return organism;
    }

    public void setOrganism(Species.Type organism) {
        this.organism = organism;
    }


    public String getProteinAccNum() {
        return proteinAccNum;
    }

    public void setProteinAccNum(String proteinAccNum) {
        this.proteinAccNum = proteinAccNum;
    }

    public Entrez getEntrezAccession() {
        return entrezAccession;
    }

    public void setEntrezAccession(Entrez entrezAccession) {
        this.entrezAccession = entrezAccession;
    }

    public int compareTo(EntrezProtRelation o) {
        return (o.getEntrezAccession().getEntrezAccNum().compareTo(this.getEntrezAccession().getEntrezAccNum()));  //To change body of implemented methods use File | Settings | File Templates.
    }
}
