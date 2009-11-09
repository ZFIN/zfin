package org.zfin.sequence.blast;

/**
 */
public interface BlastThreadCollection {
    int getTotalQueueSize();

    int getNumberQueuedNotRun();

    int getNumberRunningThreads();

    BlastQueryRunnable getNextInQueue();

    boolean isQueueActive() ;

    public int cleanCollection();
}
