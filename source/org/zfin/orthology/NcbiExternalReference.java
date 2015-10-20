package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;

public class NcbiExternalReference {

    private long ID;
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;
    private String accessionNumber;
    private ForeignDB foreignDB;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public ForeignDB getForeignDB() {
        return foreignDB;
    }

    public void setForeignDB(ForeignDB foreignDB) {
        this.foreignDB = foreignDB;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public NcbiOtherSpeciesGene getNcbiOtherSpeciesGene() {
        return ncbiOtherSpeciesGene;
    }

    public void setNcbiOtherSpeciesGene(NcbiOtherSpeciesGene ncbiGene) {
        this.ncbiOtherSpeciesGene = ncbiGene;
    }
}
