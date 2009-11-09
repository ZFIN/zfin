package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;

/**
 *  Class BlastThreadCollection.
 *
 *  This class is a singleton which holds a Collection of DoBlastThread
 *   objects so that they can be killed and their references don't get stale.
 */
public class BlastSingleQueryThreadCollection implements BlastThreadCollection{

    private final static Logger logger = Logger.getLogger(BlastSingleQueryThreadCollection.class) ;

    private Set<BlastQueryRunnable> runnableCollection = new HashSet<BlastQueryRunnable>() ;
    private static BlastSingleQueryThreadCollection instance = null ;
    private static BlastScheduler schedulerThread = null ;

    private BlastSingleQueryThreadCollection() {
        schedulerThread = new BlastScheduler();
        schedulerThread.setParent(this);
        (new Thread(schedulerThread)).start();
    }


    public synchronized static BlastSingleQueryThreadCollection getInstance(){
        if(instance==null){
            instance = new BlastSingleQueryThreadCollection() ;
        }
        return instance ;
    }

    public int getNumberRunningThreads(){
        int count = 0 ;
        for(BlastQueryRunnable blastSingleQueryThread: runnableCollection){
            if(blastSingleQueryThread.isRunning()){
                count += blastSingleQueryThread.getNumberThreads() ;
            }
        }
        return count ;
    }

    public BlastQueryRunnable getNextInQueue(){
        for(BlastQueryRunnable blastSingleQueryThread: runnableCollection){
            if(false==blastSingleQueryThread.isRunning()&&false==blastSingleQueryThread.isFinished()){
                return blastSingleQueryThread ;
            }
        }
        return null ;
    }

    /**
     *  Cleans the collection of expired threads.
     *
     */
    public int cleanCollection(){
        int collectionCleaned = 0 ;
        Iterator<BlastQueryRunnable> iter = runnableCollection.iterator() ;
        while(iter.hasNext()){
            BlastQueryRunnable blastSingleQueryThread = iter.next();
            if(blastSingleQueryThread.isFinished() ){
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
        for( BlastQueryRunnable blastSingleQueryThread : runnableCollection){
            if(blastSingleQueryThread.getXmlBlastBean().equals(xmlBlastBean)){
                return false ;
            }
        }
        return true ;
    }

    public synchronized void executeBlastThread(XMLBlastBean xmlBlastBean){

        logger.debug("sheduling blast ticket: "+ xmlBlastBean.getTicketNumber());

        ProductionBlastHeuristicFactory productionBlastHeuristicFactory = new ProductionBlastHeuristicFactory() ;
        BlastHeuristicCollection blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        BlastSingleQueryRunnable blastSingleQueryThread ;


        if(true==blastHeuristicCollection.isDoSplit()){
            blastSingleQueryThread = new BlastDistributedQueryRunnable(xmlBlastBean,blastHeuristicCollection)  ;
        }
        else{
            blastSingleQueryThread = new BlastSingleQueryRunnable(xmlBlastBean) ;
        }
        runnableCollection.add(blastSingleQueryThread) ;
    }

    public int getTotalQueueSize() {
        int count = 0 ;
        for( BlastQueryRunnable blastSingleQueryThread : runnableCollection){
                count += blastSingleQueryThread.getNumberThreads();
        }
        return count ;
    }

    public int getNumberQueuedNotRun() {
        int count = 0 ;
        for( BlastQueryRunnable blastSingleQueryThread : runnableCollection){
            if(false==blastSingleQueryThread.isFinished()&& false==blastSingleQueryThread.isRunning()){
                count += blastSingleQueryThread.getNumberThreads();
            }
        }
        return count ;
    }

    public boolean isQueueActive() {
        cleanCollection() ;
        return getNumberRunningThreads()>0 || getNumberQueuedNotRun()>0 ;
    }

    public Set<BlastQueryRunnable> getQueue() {
        return runnableCollection ;
    }
}


