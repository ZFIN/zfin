package org.zfin.orthology;

import org.zfin.sequence.ForeignDB;

public enum ExternalDatabase {
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
