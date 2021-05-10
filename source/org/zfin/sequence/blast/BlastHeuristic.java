package org.zfin.sequence.blast;

/**
 * A heuristic that indicates the number of dbsplits.
 */
public class BlastHeuristic {
    private int dbSplits ;
    private Database database ;

    public BlastHeuristic() {};


    public int getDbSplits() {
        return dbSplits;
    }

    public void setDbSplits(int dbSplits) {
        this.dbSplits = dbSplits;
    }

    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }
}
