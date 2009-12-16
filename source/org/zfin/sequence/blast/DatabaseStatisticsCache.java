package org.zfin.sequence.blast;

/**
 * This class is responsible getting database abbreviation sizes
 */
public interface DatabaseStatisticsCache {


    public DatabaseStatistics getDatabaseStatistics(Database database) throws BlastDatabaseException;

    public DatabaseStatistics getDatabaseStatistics(Database.AvailableAbbrev abbrev) throws BlastDatabaseException;

    public int clearCache();

    public int cacheAll();
}
