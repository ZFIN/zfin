package org.zfin.sequence.blast;

import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.Collection;
import java.util.Iterator;

/**
 */
public class BlastThreadService {

    public static int cleanCollection(BlastThreadCollection blastThreadCollection){
        return cleanCollection(blastThreadCollection.getQueue()) ;
    }

    public static int cleanCollection(Collection<BlastQueryJob> blastQueryJobCollection ){
        int collectionCleaned = 0 ;
        Iterator<BlastQueryJob> iter = blastQueryJobCollection.iterator() ;
        while(iter.hasNext()){
            BlastQueryJob blastSingleQueryThread = iter.next();
            if(blastSingleQueryThread.isFinished() ){
                iter.remove() ;
                ++collectionCleaned ;
            }
        }
        return collectionCleaned ;
    }

    public static int getJobCount(BlastThreadCollection blastThreadCollection) {
        return getJobCount(blastThreadCollection.getQueue()) ;
    }

    public static int getJobCount(Collection<BlastQueryJob> blastQueryJobCollection ) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            count += blastSliceThread.getNumberThreads();
        }
        return count ;
    }

    public static int getThreadCount(BlastThreadCollection blastThreadCollection) {
        return getThreadCount(blastThreadCollection.getQueue()) ;
    }

    public static int getThreadCount(Collection<BlastQueryJob> blastQueryJobCollection) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                count+= blastSliceThread.getNumberThreads();
            }
        }
        return count ;
    }

    public static int getQueuedJobCount(BlastThreadCollection blastThreadCollection) {
        return getQueuedJobCount(blastThreadCollection.getQueue()) ;
    }

    public static int getQueuedJobCount(Collection<BlastQueryJob> blastQueryJobCollection) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                ++count ;
            }
        }
        return count ;
    }

    public static int getRunningJobCount(BlastThreadCollection blastThreadCollection) {
        return getRunningJobCount(blastThreadCollection.getQueue()) ;
    }

    public static int getRunningJobCount(Collection<BlastQueryJob> blastQueryJobCollection) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                ++count ;
            }
        }
        return count ;
    }

    public static int getQueuedThreadCount(BlastThreadCollection blastThreadCollection) {
       return getQueuedThreadCount(blastThreadCollection.getQueue())  ;
    }

    public static int getQueuedThreadCount(Collection<BlastQueryJob> blastQueryJobCollection) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                count += blastSliceThread.getNumberThreads();
            }
        }
        return count ;
    }

    public static int getRunningThreadCount(BlastThreadCollection blastThreadCollection) {
        return getRunningThreadCount(blastThreadCollection.getQueue()) ;
    }

    public static int getRunningThreadCount(Collection<BlastQueryJob> blastQueryJobCollection) {
        int count = 0  ;
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(blastSliceThread.isRunning()){
                count += blastSliceThread.getNumberThreads() ;
            }
        }
        return count ;
    }

    public static BlastQueryJob getNextJobInQueue(BlastThreadCollection blastThreadCollection) {
        return getNextJobInQueue(blastThreadCollection.getQueue()) ;
    }

    public static BlastQueryJob getNextJobInQueue(Collection<BlastQueryJob> blastQueryJobCollection) {
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                return blastSliceThread ;
            }
        }
        return null ;
    }

    public static boolean isQueueActive(BlastThreadCollection blastThreadCollection) {
        return getRunningThreadCount(blastThreadCollection)>0 || getQueuedThreadCount(blastThreadCollection)>0 ;
    }

    public static boolean isQueueActive(Collection<BlastQueryJob> blastQueryJobCollection) {
        return getRunningThreadCount(blastQueryJobCollection)>0 || getQueuedThreadCount(blastQueryJobCollection)>0 ;
    }


    public static boolean isJobInQueue(XMLBlastBean xmlBlastBean,BlastThreadCollection blastThreadCollection) {
        return isJobInQueue(xmlBlastBean,blastThreadCollection.getQueue()) ;
    }

    public static boolean isJobInQueue(XMLBlastBean xmlBlastBean,Collection<BlastQueryJob> blastQueryJobCollection) {
        for(BlastQueryJob blastSliceThread: blastQueryJobCollection){
            if(false==blastSliceThread.isFinished() && blastSliceThread.getXmlBlastBean().equals(xmlBlastBean)){
                return true ;
            }
        }
        return false ;
    }

}
