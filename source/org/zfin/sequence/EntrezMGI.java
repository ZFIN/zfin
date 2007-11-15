package org.zfin.sequence;

import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.Entrez;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;


public class EntrezMGI implements Serializable {

    private Entrez entrezAccession;
    private String mgiAccession;
    private ReferenceDatabase refDB;
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    private String entrezAccessionNum;


    public String getEntrezAccessionNum() {
        return entrezAccessionNum;
    }

    public void setEntrezAccessionNum(String entrezAccessionNum) {
        this.entrezAccessionNum = entrezAccessionNum;
    }

    public Entrez getEntrezAccession() {
        return entrezAccession;
    }

    public void setEntrezAccession(Entrez entrezAccession) {
        this.entrezAccession = entrezAccession;
    }

    public String getMgiAccession() {
        return mgiAccession;
    }

    public void setMgiAccession(String mgiAccession) {
        this.mgiAccession = mgiAccession;
    }

     public ReferenceDatabase getRefDB() {
        ForeignDB mgiForeignDB = sequenceRepository.getForeignDBByName("MGI");
        ReferenceDatabase refDB =sequenceRepository.getReferenceDatabaseByAlternateKey(
                mgiForeignDB,
                ReferenceDatabase.Type.ORTHOLOGUE,
                ReferenceDatabase.SuperType.ORTHOLOGUE,
                Species.MOUSE);
         return refDB;
    }

    public void setRefDB(ReferenceDatabase refDB) {
        this.refDB = refDB;
    }
}
