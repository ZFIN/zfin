package org.zfin.sequence;

import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.Serializable;


public class EntrezMGI implements Serializable {

    private Entrez entrezAccession;
    private String mgiAccession;
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
        return sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.MGI,
                ForeignDBDataType.DataType.ORTHOLOGUE,
                ForeignDBDataType.SuperType.ORTHOLOGUE,
                Species.MOUSE);
    }

    public int hashCode() {
        int num = 39;
        if (entrezAccessionNum != null)
            num += entrezAccessionNum.hashCode();
        if (mgiAccession != null)
            num += mgiAccession.hashCode();
        return num;
    }

    /**
     * This method assumes that mgiAccession, entrezAccessionNum are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof EntrezMGI))
            return false;
        EntrezMGI mgi = (EntrezMGI) o;

        if (entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (mgi.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (mgiAccession == null)
            throw new RuntimeException("mgiAccession is null but should not!");
        if (mgi.mgiAccession == null)
            throw new RuntimeException("mgiAccession is null but should not!");

        return (entrezAccessionNum.equals(mgi.entrezAccessionNum)) &&
                (entrezAccession.equals(mgi.entrezAccession));
    }
}
