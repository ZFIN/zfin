package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;

public class NcbiOrthoExternalReference implements Comparable<NcbiOrthoExternalReference> {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NcbiOrthoExternalReference that = (NcbiOrthoExternalReference) o;

        if (referenceDatabase != null ? !referenceDatabase.equals(that.referenceDatabase) : that.referenceDatabase != null)
            return false;
        if (ncbiOtherSpeciesGene != null ? !ncbiOtherSpeciesGene.equals(that.ncbiOtherSpeciesGene) : that.ncbiOtherSpeciesGene != null)
            return false;
        return !(accessionNumber != null ? !accessionNumber.equals(that.accessionNumber) : that.accessionNumber != null);

    }

    @Override
    public int hashCode() {
        int result = referenceDatabase != null ? referenceDatabase.hashCode() : 0;
        result = 31 * result + (ncbiOtherSpeciesGene != null ? ncbiOtherSpeciesGene.hashCode() : 0);
        result = 31 * result + (accessionNumber != null ? accessionNumber.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(NcbiOrthoExternalReference o) {
        ForeignDB.AvailableName nameOne = getReferenceDatabase().getForeignDB().getDbName();
        ForeignDB.AvailableName nameTwo = o.getReferenceDatabase().getForeignDB().getDbName();
        ExternalDatabase externalDatabaseOne = ExternalDatabase.getExternalDatabase(nameOne);
        ExternalDatabase externalDatabaseTwo = ExternalDatabase.getExternalDatabase(nameTwo);
        if (externalDatabaseOne == null)
            return -1;
        if (externalDatabaseTwo == null)
            return 1;
        return externalDatabaseOne.getIndex() - externalDatabaseTwo.getIndex();

    }
}
