package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * This class is responsible getting database abbreviation sizes
 */
public class WebHostDatabaseStatisticsCache extends AbstractDatabaseStatisticsCache {

    private static DatabaseStatisticsCache localDatabaseSizeCache;
    private final static Logger logger = Logger.getLogger(WebHostDatabaseStatisticsCache.class);

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
        } else if (true == map.containsKey(database.getAbbrev())) {
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
        if (false == map.containsKey(abbrev)) {
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
        List<Database> databases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE);
        for (Database database : databases) {
//            (new DatabaseThread(database)).start();
            (new DatabaseThread(database)).run();
        }
        return map.size();
    }

    public class DatabaseThread extends Thread {
        private Database database;

        public DatabaseThread(Database database) {
            this.database = database;
        }

        @Override
        public void run() {
            try {
                DatabaseStatistics statistics = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database);
                // this may need to be turned on if run in a threaded mode.
//                HibernateUtil.closeSession();
                logger.info("Number of sequences: " + statistics.getNumSequences() + " database: " + database.getAbbrev());
            } catch (BlastDatabaseException e) {
                logger.error("Failed to cache threads", e);
            }
        }
    }
}
