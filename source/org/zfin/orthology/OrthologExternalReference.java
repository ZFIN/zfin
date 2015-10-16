package org.zfin.orthology;

import org.zfin.sequence.ReferenceDatabase;

public class OrthologExternalReference {

    private Ortholog ortholog;
    private String accessionNumber;
    private ReferenceDatabase referenceDatabase;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public Ortholog getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(Ortholog ortholog) {
        this.ortholog = ortholog;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase database) {
        this.referenceDatabase = database;
    }
}
