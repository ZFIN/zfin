package org.zfin.orthology;

import org.apache.commons.lang3.ObjectUtils;
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
        int externalDbCompare = ObjectUtils.compare(externalDatabaseOne, externalDatabaseTwo);
        if (externalDbCompare != 0) {
            return externalDbCompare;
        }
        return ObjectUtils.compare(getAccessionNumber(), o.getAccessionNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrthologExternalReference reference = (OrthologExternalReference) o;

        if (ortholog != null ? !ortholog.equals(reference.ortholog) : reference.ortholog != null) return false;
        if (accessionNumber != null ? !accessionNumber.equals(reference.accessionNumber) : reference.accessionNumber != null) {
            return false;
        }
        return !(referenceDatabase != null ? !referenceDatabase.equals(reference.referenceDatabase) : reference.referenceDatabase != null);

    }

    @Override
    public int hashCode() {
        int result = ortholog != null ? ortholog.hashCode() : 0;
        result = 31 * result + (accessionNumber != null ? accessionNumber.hashCode() : 0);
        result = 31 * result + (referenceDatabase != null ? referenceDatabase.hashCode() : 0);
        return result;
    }

}
