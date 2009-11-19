package org.zfin.sequence.blast;

import org.apache.log4j.Logger;

public class BlastScheduler implements Runnable{

    private Logger logger = Logger.getLogger(BlastScheduler.class) ;

    private BlastThreadCollection blastThreadCollection;
    private int maxRunningThreads = 80 ;

    public BlastScheduler(BlastThreadCollection blastThreadCollection){
        this.blastThreadCollection = blastThreadCollection ;
    }

    public void run() {
        if (blastThreadCollection == null) {
            logger.error("Parent must be given for SchedulerThread");
            return;
        }

        int thread = 0;
        while (BlastThreadService.isQueueActive(blastThreadCollection.getQueue())) {
            logger.debug("entering: " + thread);
            int removedFromQueue = BlastThreadService.cleanCollection(blastThreadCollection) ;
            logger.debug("finished and removed from queue: " +removedFromQueue);
            int queuedJobSize = BlastThreadService.getJobCount(blastThreadCollection);
            logger.debug("queued job Size: " + queuedJobSize);
            int numQueuedThreads = BlastThreadService.getQueuedThreadCount(blastThreadCollection);
            logger.debug("Queued threads: " + numQueuedThreads);
            int numRunningThreads = BlastThreadService.getRunningThreadCount(blastThreadCollection);
            logger.debug("Running Threads: " + numRunningThreads);
            int threadsToSchedule = ((numQueuedThreads>maxRunningThreads+numRunningThreads)? maxRunningThreads - numRunningThreads : numQueuedThreads);
            logger.debug("threadsToSchedule: " + threadsToSchedule);
            threadsToSchedule = (numRunningThreads>=maxRunningThreads ? 0 : threadsToSchedule) ;
            logger.debug("revised threadsToSchedule: " + threadsToSchedule);
            try {
                for (int scheduledThreadNumber = 0; scheduledThreadNumber < threadsToSchedule; ) {
                    BlastQueryJob blastSingleQueryJob = BlastThreadService.getNextJobInQueue(blastThreadCollection);
                    logger.debug("trying to schedule: " + blastSingleQueryJob);
                    if (blastSingleQueryJob != null) {
                        logger.debug("scheduling: " + blastSingleQueryJob.getXmlBlastBean().getTicketNumber());
                        new Thread(blastSingleQueryJob).start();
                        logger.debug("scheduled: " + blastSingleQueryJob.getXmlBlastBean().getTicketNumber());
                        scheduledThreadNumber+= blastSingleQueryJob.getNumberThreads() ;
                    }
                    else{
                        logger.error("failed to schedule blast: " + blastSingleQueryJob);
                        ++scheduledThreadNumber ;
                    }

                    // need to wait for this to begin running in order to start the next one
                    while (false == blastSingleQueryJob.isRunning() && false == blastSingleQueryJob.isFinished()) {
                        logger.debug("not started up yet, still waiting: " + blastSingleQueryJob.getXmlBlastBean().getTicketNumber());
                        logger.debug("running: " + blastSingleQueryJob.isRunning());
                        logger.debug("finished: " + blastSingleQueryJob.isFinished());
                        Thread.sleep(300);
                    }
                }
                logger.debug("sleeping: " + thread);
                ++thread;
                // reset the counter
                if(thread==10000){
                    thread = 0 ; 
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.fillInStackTrace();
                logger.error(e);
            }
        }
        BlastThreadService.cleanCollection(blastThreadCollection);
    }

    public int getMaxRunningThreads() {
        return maxRunningThreads;
    }

    public void setMaxRunningThreads(int maxRunningThreads) {
        this.maxRunningThreads = maxRunningThreads;
    }
}
