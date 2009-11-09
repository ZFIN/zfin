package org.zfin.sequence.blast;

import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class is responsible getting database abbreviation sizes
 * @deprecated Use WebHostDatabaseStatisticsCache instead.
 */
public class BlastServerDatabaseStatisticsCache extends AbstractDatabaseStatisticsCache {

    private static BlastServerDatabaseStatisticsCache blastServerDatabaseSizeCache;
    private final static Logger logger = Logger.getLogger(BlastServerDatabaseStatisticsCache.class) ;


    public static BlastServerDatabaseStatisticsCache getInstance(){
        if(blastServerDatabaseSizeCache ==null){
            blastServerDatabaseSizeCache = new BlastServerDatabaseStatisticsCache() ;
        }
        return blastServerDatabaseSizeCache;
    }

    public int cacheAll() {
        List<Database> databases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED,Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE) ;
        for(Database database: databases){
            (new DatabaseThread(database)).start();
        }
        return map.size();
    }

    public class DatabaseThread extends Thread{
        private Database database ;
        public DatabaseThread(Database database){
            this.database = database ;
        }

        @Override
        public void run() {
            try {
                DatabaseStatistics statistics = BlastServerDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database) ;
                HibernateUtil.closeSession();
                logger.info("Number of sequences: " + statistics.getNumSequences() + " database: "+database.getAbbrev());
            } catch (BlastDatabaseException e) {
                logger.error("Failed to cache threads",e);
            }
        }
    }


    public DatabaseStatistics getDatabaseStatistics(Database database) throws BlastDatabaseException{

        if(database.getOrigination().getType()==Origination.Type.GENERATED){
            logger.debug("database is generated: "+ database.getAbbrev());
            return new DatabaseStatistics() ;
        }
        else
        if(map.containsKey(database.getAbbrev())){
            logger.debug("database cached: "+ database.getAbbrev());
            return map.get(database.getAbbrev()) ;
        }
        else{
            logger.debug("database NOT cached: "+ database.getAbbrev());
            DatabaseStatistics databaseStatistics ;

            databaseStatistics = BlastServerSSHCommandWublastService.getInstance().getDatabaseStatistics(database) ;
//            databaseStatistics.setNumSequences(3);
            int numAccessions =  RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database);
            databaseStatistics.setNumAccessions(numAccessions) ;
            // cache the value
            map.put(database.getAbbrev(),databaseStatistics) ;

            return databaseStatistics ;
        }
    }

    public DatabaseStatistics getDatabaseStatistics(Database.AvailableAbbrev abbrev) throws BlastDatabaseException{
        if(map.containsKey(abbrev)){
            return map.get(abbrev) ;
        }
        else{
            Database database = RepositoryFactory.getBlastRepository().getDatabase(abbrev)  ;
            DatabaseStatistics databaseStatistics = BlastServerSSHCommandWublastService.getInstance().getDatabaseStatistics(database) ;
            int numAccessions =  RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database);
            databaseStatistics.setNumAccessions(numAccessions) ;

            // cache the value
            map.put(database.getAbbrev(),databaseStatistics) ;

            return databaseStatistics ;
        }
    }
}