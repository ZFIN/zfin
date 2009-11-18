package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.blast.BlastQueryThreadCollection;
import org.zfin.sequence.blast.BlastStatistics;
import org.zfin.sequence.blast.BlastThreadService;

/**
 */
public class BlastJobsBean {

    private BlastQueryThreadCollection blastThreadCollection;
    private BlastStatistics blastStatistics ;

    public BlastQueryThreadCollection getBlastThreadCollection() {
        return blastThreadCollection;
    }

    public void setBlastThreadCollection(BlastQueryThreadCollection blastThreadCollection) {
        this.blastThreadCollection = blastThreadCollection;
    }

    public BlastStatistics getBlastStatistics() {
        return blastStatistics;
    }

    public void setBlastStatistics(BlastStatistics blastStatistics) {
        this.blastStatistics = blastStatistics;
    }

    public int getJobCount(){
        return BlastThreadService.getJobCount(blastThreadCollection) ;
    }


    public int getRunningJobCount(){
        return BlastThreadService.getRunningJobCount(blastThreadCollection) ;
    }


    public int getRunningThreadCount(){
        return BlastThreadService.getRunningThreadCount(blastThreadCollection) ;
    }


    public int getQueuedJobCount(){
        return BlastThreadService.getQueuedJobCount(blastThreadCollection) ;
    }


    public int getQueuedThreadCount(){
        return BlastThreadService.getQueuedThreadCount(blastThreadCollection) ;
    }
}
