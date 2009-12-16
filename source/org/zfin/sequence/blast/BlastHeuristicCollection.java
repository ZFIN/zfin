package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.List;

/**
 */
public class BlastHeuristicCollection {

    private final Logger logger = Logger.getLogger(BlastHeuristicCollection.class);

    private List<BlastHeuristic> blastHeuristics;

    public boolean isDoSplit() {
        if (CollectionUtils.isEmpty(blastHeuristics)) {
            return false;
        } else if (blastHeuristics.size() > 1) {
            return true;
        }
        // in the case of 1
        else {
            return blastHeuristics.get(0).getDbSplits() > 1;
        }
    }

    public int getNumChunksForDatabase(Database database) {
        if (true == CollectionUtils.isEmpty(blastHeuristics)) {
            return 0;
        } else {
            // todo: need to fix this with a map
            for (BlastHeuristic blastHeuristic : blastHeuristics) {
                if (blastHeuristic.getDatabase().getAbbrev() == database.getAbbrev()) {
                    return blastHeuristic.getDbSplits();
                }
            }
            return 0;
        }
    }

    public int getTotalSize() {
        if (CollectionUtils.isEmpty(blastHeuristics)) {
            return 0;
        } else {
            int returnSize = 0;
            for (BlastHeuristic blastHeuristic : blastHeuristics) {
                try {
                    returnSize += WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(blastHeuristic.getDatabase()).getNumSequences();
                } catch (BlastDatabaseException e) {
                    logger.error(e);
                }
            }
            return returnSize;
        }
    }

    public List<BlastHeuristic> getBlastHeuristics() {
        return blastHeuristics;
    }

    public void setBlastHeuristics(List<BlastHeuristic> blastHeuristics) {
        this.blastHeuristics = blastHeuristics;
    }
}
