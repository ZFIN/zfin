package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.framework.HibernateUtil;

import java.util.HashSet;
import java.util.Iterator;

/**
 *  Class BlastThreadCollection.
 *
 *  This class is a singleton which holds a Collection of DoBlastThread
 *   objects so that they can be killed and their references don't get stale.
 */
public class BlastSingleQueryThreadCollection extends HashSet<BlastSingleQueryThread>{

    private static BlastSingleQueryThreadCollection instance = null ;

    private BlastSingleQueryThreadCollection() {

    }

    /**
     *  Cleans the collection of expired threads.
     *
     */
    public int cleanCollection(){
        int collectionCleaned = 0 ;
        Iterator<BlastSingleQueryThread> iter = this.iterator() ;
        while(iter.hasNext()){
            if(false==iter.next().isAlive()){
                iter.remove() ;
                ++collectionCleaned ;
            }
        }

        return collectionCleaned ;
    }

    /**
     *  Returns if the blast result has been processed first, ie, it has been eliminated.
     */
    public boolean isBlastThreadDone(XMLBlastBean xmlBlastBean){
        cleanCollection() ;
        for( BlastSingleQueryThread blastSingleQueryThread : this){
            if(blastSingleQueryThread.getXmlBlastBean().equals(xmlBlastBean)){
                return false ;
            }
        }
        return true ;
    }

    public void executeBlastThread(XMLBlastBean xmlBlastBean){

        ProductionBlastHeuristicFactory productionBlastHeuristicFactory = new ProductionBlastHeuristicFactory() ;
        BlastHeuristicCollection blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        BlastSingleQueryThread blastSingleQueryThread ;


        if(true==blastHeuristicCollection.isDoSplit()){
            blastSingleQueryThread = new BlastDistributedQueryThread(xmlBlastBean,blastHeuristicCollection)  ;
        }
        else{
            blastSingleQueryThread = new BlastSingleQueryThread(xmlBlastBean) ;
        }
        add(blastSingleQueryThread) ;
        blastSingleQueryThread.start();
    }

    public static BlastSingleQueryThreadCollection getInstance(){
        if(instance==null){
            instance = new BlastSingleQueryThreadCollection() ;
        }
        return instance ;
    }

}


