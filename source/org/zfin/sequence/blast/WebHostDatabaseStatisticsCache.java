package org.zfin.sequence.blast;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Map;

/**
 * This class is responsible getting database abbreviation sizes
 */
public class WebHostDatabaseStatisticsCache extends AbstractDatabaseStatisticsCache {

    private static DatabaseStatisticsCache localDatabaseSizeCache;
    private final static Logger logger = LogManager.getLogger(WebHostDatabaseStatisticsCache.class);
    private boolean isCached = false;

    public static DatabaseStatisticsCache getInstance() {
        if (localDatabaseSizeCache == null) {
            localDatabaseSizeCache = new WebHostDatabaseStatisticsCache();
        }
        return localDatabaseSizeCache;
    }

    public DatabaseStatistics getDatabaseStatistics(Database database) throws BlastDatabaseException {
        if (database.getOrigination().getType() == Origination.Type.GENERATED) {
            logger.debug("database is generated: " + database.getAbbrev());
            return new DatabaseStatistics();
        }
        else
        if (map.containsKey(database.getAbbrev())) {
            logger.debug("database cached: " + database.getAbbrev());
            return map.get(database.getAbbrev());
        } else {
            logger.debug("database NOT cached: " + database.getAbbrev());
            DatabaseStatistics databaseStatistics = MountedWublastBlastService.getInstance().getDatabaseStatistics(database);
            int numAccessions = RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database);
            databaseStatistics.setNumAccessions(numAccessions);
            // cache the value
            map.put(database.getAbbrev(), databaseStatistics);

            return databaseStatistics;
        }
    }


    public DatabaseStatistics getDatabaseStatistics(Database.AvailableAbbrev abbrev) throws BlastDatabaseException {
        if (map.containsKey(abbrev)) {
            return map.get(abbrev);
        } else {

            Database database = RepositoryFactory.getBlastRepository().getDatabase(abbrev);
            int numberOfSequences = MountedWublastBlastService.getInstance().getDatabaseStatistics(database).getNumSequences();


            DatabaseStatistics databaseStatistics = new DatabaseStatistics();
            databaseStatistics.setNumSequences(numberOfSequences);
            int numAccessions = RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database);
            databaseStatistics.setNumAccessions(numAccessions);

            // cache the value
            map.put(database.getAbbrev(), databaseStatistics);

            return databaseStatistics;
        }
    }

    /**
     * Caches all of the database statistics.  Running as non-threaded as we don't want to blow out the server.
     *
     * @return The number of cached databases.
     */
    public int cacheAll() {

        logger.debug("starting cache of all blast database accession counts");
        Map<String, Integer> accessionCounts = RepositoryFactory.getBlastRepository().getValidAccessionCountsForAllBlastDatabases();

        List<Database> databases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE);
        for (Database database : databases) {
            Integer accessionCount = accessionCounts.get(database.getAbbrev().toString());
//            (new DatabaseThread(database)).start();
            (new DatabaseThread(database, accessionCount)).run();
        }

        logger.debug("finishing cache of all blast database accession counts");

        isCached = true;

        return map.size();
    }

    public class DatabaseThread extends Thread {
        private Database database;
        private Integer accessionCount;

        public DatabaseThread(Database database, Integer accessionCount) {
            this.database = database;
            this.accessionCount = accessionCount;
        }

        @Override
        public void run() {
            try {
                if (database == null)
                    logger.debug("database is null");
                logger.debug("database: " + database.getAbbrev() + ", " + database.getZdbID());
                DatabaseStatistics statistics = MountedWublastBlastService.getInstance().getDatabaseStatistics(database);

                if (statistics == null) {
                    logger.debug("statistics was null for " + database.getAbbrev() + ", " + database.getZdbID());
                } else {
                    if (accessionCount == null)
                        logger.debug("accessionCount was null");
                    else
                        statistics.setNumAccessions(accessionCount);
//                HibernateUtil.closeSession();
                logger.info("Number of sequences: " + statistics.getNumSequences() + " database: " + database.getAbbrev());

                }
                map.put(database.getAbbrev(), statistics);
                // this may need to be turned on if run in a threaded mode.

            } catch (BlastDatabaseException e) {
                logger.error("Failed to cache threads", e);
            }
        }
    }

    public boolean isCached() {
        return isCached;
    }


}
