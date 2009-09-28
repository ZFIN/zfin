package org.zfin.sequence.blast;

import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * A heuristic factory for testing blast.
 * see case 3970 for more details as to the settings
 * see case 4312 where we updated to use a logarithm instead, which was more accurate and didn't saturate the blast.
 * numSplits = 0.5285 * ln(databaseSize) - 3.2453
 *  . . . using 0.53 * ln(databaseSize)-3.25
 * @link http://zfinwinserver1.uoregon.edu/fogbugz/default.asp?3970
 */
public class ProductionBlastHeuristicFactory extends BlastHeuristicFactory{

    private final Logger logger = Logger.getLogger(ProductionBlastHeuristicFactory.class) ;


    private int maxSegs = 10 ;  // the maximum # of segments we can create
    private float splitSequenceValue = 0.53f ;  // the size we need in order to split the database
    private float intercept = -3.25f;  // the size we need in order to split the database

    protected BlastHeuristic createBlastHeuristic(Database database) {
        BlastHeuristic blastHeuristic = new BlastHeuristic() ;
        int dbsplits = 1 ;
        int databaseSize = 1 ;
        try {
            databaseSize = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database).getNumSequences() ;
            dbsplits = (int) Math.floor(splitSequenceValue *Math.log(databaseSize) + intercept) ;

            // only need to handle the EXCEEDS case
            if(dbsplits>maxSegs){
                dbsplits = maxSegs ;
            }
            else
            if(dbsplits < 1 ){
                dbsplits = 1 ;
            }
        } catch (BlastDatabaseException io){
            logger.error("failed to get the size of the database: " + database);
        }
        blastHeuristic.setDatabase(database);
        blastHeuristic.setDbSplits(dbsplits);
        return blastHeuristic ;
    }


    public int getMaxSegs() {
        return maxSegs;
    }

    public void setMaxSegs(int maxSegs) {
        this.maxSegs = maxSegs;
    }

    public float getSplitSequenceValue() {
        return splitSequenceValue;
    }

    public void setSplitSequenceValue(float splitSequenceValue) {
        this.splitSequenceValue = splitSequenceValue;
    }

    public float getIntercept() {
        return intercept;
    }

    public void setIntercept(float intercept) {
        this.intercept = intercept;
    }
}