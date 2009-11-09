package org.zfin.sequence.blast.presentation;

import org.zfin.sequence.blast.BlastQueryRunnable;

import java.util.Set;

/**
 */
public class BlastJobsBean {

    Set<BlastQueryRunnable> blastJobs ;

    public Set<BlastQueryRunnable> getBlastJobs() {
        return blastJobs;
    }

    public void setBlastJobs(Set<BlastQueryRunnable> blastJobs) {
        this.blastJobs = blastJobs;
    }

    public int getNumJobsRunning() {
        int numJobsRunning = 0 ;
        for(BlastQueryRunnable blastQueryRunnable: blastJobs){
            if(blastQueryRunnable.isRunning()){
                ++numJobsRunning ;
            }
        }
        return numJobsRunning ;
    }

    public int getNumThreadsRunning(){
        int count = 0 ;
        for(BlastQueryRunnable blastQueryRunnable: blastJobs){
            if(blastQueryRunnable.isRunning()){
                count += blastQueryRunnable.getNumberThreads();
            }
        }
        return count ;
    }

    public int getNumJobsQueued() {
        int numJobsQueued= 0 ;
        for(BlastQueryRunnable blastQueryRunnable: blastJobs){
            if(false==blastQueryRunnable.isRunning()&& false==blastQueryRunnable.isFinished()){
                ++numJobsQueued;
            }
        }
        return numJobsQueued ;
    }

    public int getNumThreadsQueued() {
        int count = 0 ;
        for(BlastQueryRunnable blastQueryRunnable: blastJobs){
            if(false==blastQueryRunnable.isRunning()&& false==blastQueryRunnable.isFinished()){
                count += blastQueryRunnable.getNumberThreads() ;
            }
        }
        return count ;
    }
}
