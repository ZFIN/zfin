package org.zfin.sequence.blast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible getting database abbreviation sizes
 */
public abstract class AbstractDatabaseStatisticsCache implements DatabaseStatisticsCache {

    protected Map<Database.AvailableAbbrev,DatabaseStatistics> map = new ConcurrentHashMap<Database.AvailableAbbrev,DatabaseStatistics>() ;

    public int clearCache(){
        int returnValue = map.size() ;
        map.clear();
        return returnValue ;
    }
}