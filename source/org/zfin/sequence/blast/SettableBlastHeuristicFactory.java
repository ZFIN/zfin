package org.zfin.sequence.blast;

/**
 * A heuristic factory for testing blast.
 */
public class SettableBlastHeuristicFactory extends BlastHeuristicFactory{

    private int testDBSplits = 0 ;
    private boolean doSplit = false  ;

    public SettableBlastHeuristicFactory(int dbSplit,boolean doSplit){
        this.testDBSplits = dbSplit;
        this.doSplit = doSplit;
    }

    protected BlastHeuristic createBlastHeuristic(Database database) {
        BlastHeuristic blastHeuristic = new BlastHeuristic();
        blastHeuristic.setDatabase(database);
        blastHeuristic.setDbSplits(testDBSplits);
        return blastHeuristic ;
    }

}
