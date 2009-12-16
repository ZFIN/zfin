package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Class BlastThreadCollection.
 * <p/>
 * This class is a singleton which holds a Collection of DoBlastThread
 * objects so that they can be killed and their references don't get stale.
 */
public class BlastQueryThreadCollection implements BlastThreadCollection {

    private final static Logger logger = Logger.getLogger(BlastQueryThreadCollection.class);

    private Collection<BlastQueryJob> jobCollection = new ConcurrentLinkedQueue<BlastQueryJob>();
    private static BlastQueryThreadCollection instance = null;
    private static BlastScheduler blastScheduler = null;
    private static ThreadFactory blastThreadFactory = Executors.defaultThreadFactory();
    private static Thread blastSchedulerThread = null;

    private BlastQueryThreadCollection() {
        blastScheduler = new BlastScheduler(this);
    }


    public synchronized static BlastQueryThreadCollection getInstance() {
        if (instance == null) {
            instance = new BlastQueryThreadCollection();
        }
        return instance;
    }

    public Collection<BlastQueryJob> getQueue() {
        return jobCollection;
    }


    /**
     * Returns if the blast result has been processed first, ie, it has been eliminated.
     */
    public boolean isBlastThreadDone(XMLBlastBean xmlBlastBean) {
        BlastThreadService.cleanCollection(jobCollection);
        for (BlastQueryJob blastSingleQueryThread : jobCollection) {
            if (blastSingleQueryThread.getXmlBlastBean().equals(xmlBlastBean)) {
                return false;
            }
        }
        return true;
    }

    public synchronized void executeBlastThread(XMLBlastBean xmlBlastBean) {
        logger.debug("sheduling blast ticket: " + xmlBlastBean.getTicketNumber());
        BlastHeuristicFactory productionBlastHeuristicFactory = new ProductionBlastHeuristicFactory();
        BlastHeuristicCollection blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean);
        BlastQueryJob blastSingleTicketQueryThread
                = new BlastDistributableQueryThread(xmlBlastBean, blastHeuristicCollection);
        jobCollection.add(blastSingleTicketQueryThread);

        startScheduler();
    }

    private void startScheduler() {
        if (blastSchedulerThread == null || false == blastSchedulerThread.isAlive()) {
            blastSchedulerThread = null; // a little redundant
            blastSchedulerThread = blastThreadFactory.newThread(blastScheduler);
            blastSchedulerThread.start();
        }
    }

    public static BlastScheduler getBlastScheduler() {
        return blastScheduler;
    }
}


