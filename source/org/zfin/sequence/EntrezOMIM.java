package org.zfin.sequence;

import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.io.Serializable;


public class EntrezOMIM implements Serializable {

    private Entrez entrezAccession;
    private String omimAccession;
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
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

    public int hashCode() {
        int num = 39;
        if (omimAccession != null)
            num += omimAccession.hashCode();
        if (entrezAccessionNum != null)
            num += entrezAccessionNum.hashCode();
        if (entrezAccession != null)
            num += entrezAccession.hashCode();
        return num;
    }

    /**
     * This method assumes that omimAccession, omimAccessionNum and entrezAccession are not null.
     * Otherwise this method throws an exception.
     *
     * @param o Object
     * @return boolean
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof EntrezOMIM))
            return false;
        EntrezOMIM omim = (EntrezOMIM) o;

        if (omimAccession == null)
            throw new RuntimeException("omimAccession is null but should not!");
        if (omim.omimAccession == null)
            throw new RuntimeException("omimAccession is null but should not!");
        if (entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (omim.entrezAccessionNum == null)
            throw new RuntimeException("entrezAccessionNum is null but should not!");
        if (entrezAccession == null)
            throw new RuntimeException("entrezAccession is null but should not!");
        if (omim.entrezAccession == null)
            throw new RuntimeException("entrezAccession is null but should not!");

        return omimAccession.equals(omim.omimAccession) &&
                (entrezAccessionNum.equals(omim.entrezAccessionNum)) &&
                (entrezAccession.equals(omim.entrezAccession));
    }
}
