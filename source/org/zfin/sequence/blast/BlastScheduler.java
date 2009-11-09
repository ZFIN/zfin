package org.zfin.sequence.blast;

import org.apache.log4j.Logger;

public class BlastScheduler implements Runnable{

    private Logger logger = Logger.getLogger(BlastScheduler.class) ;

    private BlastThreadCollection parent;
    private int maxRunningThreads = 4 ;

    public void setParent(BlastThreadCollection parent) {
        this.parent = parent;
    }

    public void run() {
        if (parent == null) {
            logger.error("Parent must be given for SchedulerThread");
            return;
        }

        int thread = 0;
        while (true) {
            logger.debug("entering: " + thread);
            int removedFromQueue = parent.cleanCollection() ;
            logger.debug("finished and removed from queue: " +removedFromQueue);
            int queueSize = parent.getTotalQueueSize();
            logger.debug("queueSize: " + queueSize);
            int numQueued = parent.getNumberQueuedNotRun();
            logger.debug("QueuedNotRun: " + numQueued);
            int numRunningThreads = parent.getNumberRunningThreads();
            logger.debug("numRunningThreads: " + numRunningThreads);
            int threadsToSchedule = (numQueued>numRunningThreads ? numQueued - numRunningThreads:0);
            logger.debug("threadsToSchedule: " + threadsToSchedule);
            threadsToSchedule = (threadsToSchedule+numRunningThreads>maxRunningThreads ? maxRunningThreads-numRunningThreads: threadsToSchedule) ;
            logger.debug("revised threadsToSchedule: " + threadsToSchedule);
            try {
                for (int scheduledThreadNumber = 0; scheduledThreadNumber < threadsToSchedule; ) {
                    BlastQueryRunnable blastSingleQueryRunnable = parent.getNextInQueue();
                    logger.debug("trying to schedule: " + blastSingleQueryRunnable);
                    if (blastSingleQueryRunnable != null) {
                        logger.debug("scheduling: " + blastSingleQueryRunnable.getXmlBlastBean().getTicketNumber());
                        new Thread(blastSingleQueryRunnable).start();
                        logger.debug("scheduled: " + blastSingleQueryRunnable.getXmlBlastBean().getTicketNumber());
                        scheduledThreadNumber+=blastSingleQueryRunnable.getNumberThreads() ;
                    }
                    else{
                        logger.error("failed to schedule blast: " + blastSingleQueryRunnable);
                        ++scheduledThreadNumber ;
                    }

                    // need to wait for this to begin running in order to start the next one
                    while (false == blastSingleQueryRunnable.isRunning() && false == blastSingleQueryRunnable.isFinished()) {
                        logger.debug("not started up yet, still waiting: " + blastSingleQueryRunnable.getXmlBlastBean().getTicketNumber());
                        logger.debug("running: " + blastSingleQueryRunnable.isRunning());
                        logger.debug("finished: " + blastSingleQueryRunnable.isFinished());
                        Thread.sleep(200);
                    }
                }
                logger.debug("sleeping: " + thread);
                ++thread;
                if(thread==10000){
                    thread = 0 ; 
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.fillInStackTrace();
                logger.error(e);
            }
        }
    }

    public int getMaxRunningThreads() {
        return maxRunningThreads;
    }

    public void setMaxRunningThreads(int maxRunningThreads) {
        this.maxRunningThreads = maxRunningThreads;
    }
}