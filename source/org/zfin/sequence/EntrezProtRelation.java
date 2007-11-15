package org.zfin.sequence;

import org.zfin.sequence.Accession;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Entrez;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;


public class EntrezProtRelation implements Comparable<EntrezProtRelation>{

    private long epID;
    private Species organism;
    private String proteinAccNum;
    private Entrez entrezAccession;
    private ReferenceDatabase humanrefDB;
    private ReferenceDatabase mouserefDB;
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();



    public long getEpID() {
        return epID;
    }

    public void setEpID(long epID) {
        this.epID = epID;
    }


    public Species getOrganism() {
        return organism;
    }

    public void setOrganism(Species organism) {
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


    public ReferenceDatabase getHumanrefDB() {
        ForeignDB entrezForeignDB = sequenceRepository.getForeignDBByName("Entrez Gene");
        ReferenceDatabase humanrefDB =sequenceRepository.getReferenceDatabaseByAlternateKey(
                entrezForeignDB,
                ReferenceDatabase.Type.ORTHOLOGUE,
                ReferenceDatabase.SuperType.ORTHOLOGUE,
                Species.HUMAN);
         return humanrefDB;
    }

    public void setHumanrefDB(ReferenceDatabase humanrefDB) {
        this.humanrefDB = humanrefDB;
    }

    public ReferenceDatabase getMouserefDB() {
        ForeignDB entrezForeignDB = sequenceRepository.getForeignDBByName("Entrez Gene");
        ReferenceDatabase mouserefDB =sequenceRepository.getReferenceDatabaseByAlternateKey(
                entrezForeignDB,
                ReferenceDatabase.Type.ORTHOLOGUE,
                ReferenceDatabase.SuperType.ORTHOLOGUE,
                Species.MOUSE);
         return mouserefDB;
    }

    public void setMouserefDB(ReferenceDatabase mouserefDB) {
        this.mouserefDB = mouserefDB;
    }

    public int compareTo(EntrezProtRelation o) {
        return (o.getEntrezAccession().getEntrezAccNum().compareTo(this.getEntrezAccession().getEntrezAccNum()));  //To change body of implemented methods use File | Settings | File Templates.
    }
}
