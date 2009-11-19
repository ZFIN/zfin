package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastOutputMerger;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Class BlastDistributedQueryThread is a wrapper for a set of blast operations for a single
 * query distributed:
 * 1. multiple databases
 * 2. splitting single databases into smaller parts
 */
public class BlastDistributableQueryThread extends AbstractQueryThread implements BlastThreadCollection{

    private final static Logger logger = Logger.getLogger(BlastDistributableQueryThread.class);

    private Collection<BlastQueryJob> blastSlices = new ConcurrentLinkedQueue<BlastQueryJob>();
    private BlastHeuristicCollection blastHeuristicCollection;
    private int totalThreads = 1 ;
    private int threadSubmitLatency = 200 ;
    private int checkJobLatency = 200 ;

    public BlastDistributableQueryThread(XMLBlastBean xmlBlastBean, BlastHeuristicCollection blastHeuristicCollection) {
        super(xmlBlastBean);
        this.blastHeuristicCollection = blastHeuristicCollection;

        calculateTotalThreads();
    }

    private int calculateTotalThreads() {
        totalThreads = 0  ;
        List<Database> databases = xmlBlastBean.getActualDatabaseTargets();
        for (Database database : databases) {
            int numChunks = blastHeuristicCollection.getNumChunksForDatabase(database);
            if (numChunks < 1) {
                numChunks = 1;
            }
            totalThreads += numChunks ;
        }
        return totalThreads ;
    }

    public int getNumberThreads() {
        return totalThreads ;
    }

    // todo: note that we can  not return until we are done running
    public void run() {

        startBlast();
        try {
// 1. first split on each database
            // for each database
            List<Database> databases = xmlBlastBean.getActualDatabaseTargets();

//         remember we are going to re-assble these later
            for (Database database : databases) {
                int numChunks = blastHeuristicCollection.getNumChunksForDatabase(database);
                if (numChunks < 1) {
                    numChunks = 1;
                }

                // 2. create chunks for each and execute
                for (int i = 0; i < numChunks; i++) {
                    XMLBlastBean sliceBean = xmlBlastBean.clone();
                    sliceBean.setSliceNumber(i);
                    sliceBean.setNumChunks(numChunks);

                    BlastSliceThread blastSliceThread = new BlastSliceThread(sliceBean, database, i);
                    blastSlices.add(blastSliceThread);
                    (new Thread(blastSliceThread)).start();
                    try {
                        Thread.sleep(threadSubmitLatency);
                    } catch (InterruptedException e) {
                        logger.error(e);
                    }
                }
            }


            // sleep a few seconds each time
            while (BlastThreadService.isJobInQueue(xmlBlastBean,blastSlices)) {
                try {
                    Thread.sleep(checkJobLatency);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }


            BlastOutput blastOutput = BlastOutputMerger.mergeBlastOutput(blastSlices);
            xmlBlastBean.setBlastOutput(blastOutput);
            xmlBlastBean.setBlastResultBean(BlastResultMapper.createBlastResultBean(blastOutput));

            // the thread is done with the database so close the databse connection
            HibernateUtil.closeSession();


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(xmlBlastBean.getResultFile()));
                if (blastOutput != null) {
                    Marshaller u = getJAXBBlastContext().createMarshaller();
                    u.marshal(blastOutput, new BufferedWriter(writer));
                } else {
                    writer.write(xmlBlastBean.getErrorString());
                }
                writer.close();
            } catch (Exception e) {
                logger.fatal("Failed to write blast result to file" , e.fillInStackTrace());
            }
        } finally {
            finishBlast();
        }
    }


    public Collection<BlastQueryJob> getQueue() {
        return blastSlices;
    }

}
