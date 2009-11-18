package org.zfin.sequence.blast;

import java.util.Date;

/**
 */
public class BlastStatistics {

    private static BlastStatistics instance ;

    // statistics
    private Date startTime = new Date() ;

    private long runTime = 0 ; /// run time includes ALL threads. time in milliseconds.
    private long queueTime = 0 ; /// queue time includes ALL threads. time in milliseconds.
    private long numThreads = 0 ;
    private long numJobs = 0 ;


    private BlastStatistics(){}

    public static BlastStatistics getInstance(){
        if(instance==null){
            instance = new BlastStatistics();
        }
        return instance ;
    }

    public void recordStatistics(BlastQueryJob blastQueryJob){
        ++numJobs  ;
        numThreads += blastQueryJob.getNumberThreads();
        runTime += blastQueryJob.getFinishTime().getTime()-blastQueryJob.getStartTime().getTime();
        queueTime += blastQueryJob.getStartTime().getTime()-blastQueryJob.getQueueTime().getTime();
    }

    public Date getStartTime() {
        return startTime;
    }

    public long getRunTime() {
        return runTime;
    }

    public long getQueueTime() {
        return queueTime;
    }

    public long getNumThreads() {
        return numThreads;
    }

    public long getNumJobs() {
        return numJobs;
    }

    public void clear(){
        runTime = 0 ;
        queueTime = 0 ;
        numThreads = 0 ;
        numJobs = 0 ; 
    }
}
