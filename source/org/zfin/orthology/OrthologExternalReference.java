package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;

public class OrthologExternalReference implements Comparable<OrthologExternalReference> {

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

    @Override
    public int compareTo(OrthologExternalReference o) {
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrthologExternalReference reference = (OrthologExternalReference) o;

        if (ortholog != null ? !ortholog.equals(reference.ortholog) : reference.ortholog != null) return false;
        if (accessionNumber != null ? !accessionNumber.equals(reference.accessionNumber) : reference.accessionNumber != null)
            return false;
        return !(referenceDatabase != null ? !referenceDatabase.equals(reference.referenceDatabase) : reference.referenceDatabase != null);

    }

    @Override
    public int hashCode() {
        int result = ortholog != null ? ortholog.hashCode() : 0;
        result = 31 * result + (accessionNumber != null ? accessionNumber.hashCode() : 0);
        result = 31 * result + (referenceDatabase != null ? referenceDatabase.hashCode() : 0);
        return result;
    }

    enum ExternalDatabase {
        MGI(0, ForeignDB.AvailableName.MGI),
        FLYBASE(1, ForeignDB.AvailableName.FLYBASE),
        GENE(10, ForeignDB.AvailableName.GENE),
        OMIM(11, ForeignDB.AvailableName.OMIM),
        HGNC(12, ForeignDB.AvailableName.HGNC);

        private ForeignDB.AvailableName database;
        private int index;

        ExternalDatabase(int index, ForeignDB.AvailableName database) {
            this.index = index;
            this.database = database;
        }

        public int getIndex() {
            return index;
        }

        public static ExternalDatabase getExternalDatabase(ForeignDB.AvailableName name) {
            for (ExternalDatabase externalDB : values()) {
                if (externalDB.database.equals(name))
                    return externalDB;
            }
            return null;
        }
    }

}
