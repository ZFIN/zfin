package org.zfin.sequence;

import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.Entrez;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;

import java.io.Serializable;


public class EntrezOMIM implements Serializable {

    private Entrez entrezAccession;
    private String  omimAccession;
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

    public String getOmimAccession() {
        return omimAccession;
    }

    public void setOmimAccession(String omimAccession) {
        this.omimAccession = omimAccession;
    }



    public ReferenceDatabase getRefDB() {
           ForeignDB omimForeignDB = sequenceRepository.getForeignDBByName("OMIM");
           ReferenceDatabase refDB =sequenceRepository.getReferenceDatabaseByAlternateKey(
                   omimForeignDB,
                   ReferenceDatabase.Type.ORTHOLOGUE,
                   ReferenceDatabase.SuperType.ORTHOLOGUE,
                   Species.HUMAN);
            return refDB;
       }


    public void setRefDB(ReferenceDatabase refDB) {
        this.refDB = refDB;
    }
    
}
