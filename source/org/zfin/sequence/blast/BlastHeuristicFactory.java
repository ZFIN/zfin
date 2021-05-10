package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.ArrayList;
import java.util.List;

/**
 * A factory that creates blast heuristics from a XMLBlastBean
 */
public abstract class BlastHeuristicFactory {
    protected abstract BlastHeuristic createBlastHeuristic(Database database) ;

    public BlastHeuristicCollection createBlastHeuristics(XMLBlastBean xmlBlastBean) {
        List<BlastHeuristic> blastHeuristics = new ArrayList<BlastHeuristic>() ;
        List<Database> databases = xmlBlastBean.getActualDatabaseTargets() ;
        for(Database database : databases){
            BlastHeuristic blastHeuristic = createBlastHeuristic(database) ;
            blastHeuristics.add(blastHeuristic) ;
        }

        BlastHeuristicCollection blastHeuristicCollection = new BlastHeuristicCollection();
        blastHeuristicCollection.setBlastHeuristics(blastHeuristics);
        return blastHeuristicCollection;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
