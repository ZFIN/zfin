package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;

import java.util.Comparator;

public class OrthologAccessionSorting implements Comparator<OrthologExternalReference> {


    @Override
    public int compare(OrthologExternalReference o1, OrthologExternalReference o2) {
        ForeignDB.AvailableName nameOne = o1.getReferenceDatabase().getForeignDB().getDbName();
        ForeignDB.AvailableName nameTwo = o2.getReferenceDatabase().getForeignDB().getDbName();
        ExternalDatabase externalDatabaseOne = ExternalDatabase.getExternalDatabase(nameOne);
        ExternalDatabase externalDatabaseTwo = ExternalDatabase.getExternalDatabase(nameTwo);
        if (externalDatabaseOne == null)
            return -1;
        if (externalDatabaseTwo == null)
            return 1;
        return externalDatabaseOne.getIndex() - externalDatabaseTwo.getIndex();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
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
