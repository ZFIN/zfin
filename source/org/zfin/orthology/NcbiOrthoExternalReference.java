package org.zfin.orthology;

import org.zfin.sequence.ReferenceDatabase;

public class NcbiOrthoExternalReference {

    private long ID;
    private ReferenceDatabase referenceDatabase;
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;
    private String accessionNumber;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public NcbiOtherSpeciesGene getNcbiOtherSpeciesGene() {
        return ncbiOtherSpeciesGene;
    }

    public void setNcbiOtherSpeciesGene(NcbiOtherSpeciesGene ncbiOtherSpeciesGene) {
        this.ncbiOtherSpeciesGene = ncbiOtherSpeciesGene;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }
}
