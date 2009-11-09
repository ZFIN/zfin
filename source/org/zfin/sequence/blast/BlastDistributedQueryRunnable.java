package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.framework.HibernateUtil;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastOutputMerger;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;


/**
 * Class BlastDistributedQueryThread is a wrapper for a set of blast operations for a single
 * query distributed:
 * 1. multiple databases
 * 2. splitting single databases into smaller parts
 */
public class BlastDistributedQueryRunnable extends BlastSingleQueryRunnable implements BlastThreadCollection{

    private final static Logger logger = Logger.getLogger(BlastDistributedQueryRunnable.class);

    private List<BlastSliceThread> blastSlices = new ArrayList<BlastSliceThread>();
    private BlastHeuristicCollection blastHeuristicCollection;
    private int totalThreads = 1 ;

    public BlastDistributedQueryRunnable(XMLBlastBean xmlBlastBean, BlastHeuristicCollection blastHeuristicCollection) {
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
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        }


        // sleep a few seconds each time
        while (true == isQueueActive()) {
            try {
                Thread.sleep(200);
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
                JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
                Marshaller u = jc.createMarshaller();
                u.marshal(blastOutput, new BufferedWriter(writer));
            } else {
                writer.write(xmlBlastBean.getErrorString());
            }
            writer.close();
        } catch (Exception e) {
            logger.fatal("Failed to write blast result to file: " + e);
        }
        finishBlast();
    }

    public int cleanCollection(){
        int collectionCleaned = 0 ;
        Iterator<BlastSliceThread> iter = blastSlices.iterator() ;
        while(iter.hasNext()){
            BlastQueryRunnable blastSingleQueryThread = iter.next();
            if(blastSingleQueryThread.isFinished() ){
                iter.remove() ;
                ++collectionCleaned ;
            }
        }
        return collectionCleaned ;
    }


    public int getTotalQueueSize() {
        int count = 0  ;
        for(BlastSliceThread blastSliceThread: blastSlices){
                count += blastSliceThread.getNumberThreads();
        }
        return count ;
    }

    public int getNumberQueuedNotRun() {
        int count = 0  ;
        for(BlastSliceThread blastSliceThread: blastSlices){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                count += blastSliceThread.getNumberThreads();
            }
        }
        return count ;
    }

    public int getNumberRunningThreads() {
        int count = 0  ;
        for(BlastSliceThread blastSliceThread: blastSlices){
            if(blastSliceThread.isRunning()){
                count += blastSliceThread.getNumberThreads() ;
            }
        }
        return count ;
    }

    public BlastQueryRunnable getNextInQueue() {
        for(BlastSliceThread blastSliceThread: blastSlices){
            if(false==blastSliceThread.isFinished()&& false==blastSliceThread.isRunning()){
                return blastSliceThread ;
            }
        }
        return null ;
    }

    public boolean isQueueActive() {
        return getNumberRunningThreads()>0 || getNumberQueuedNotRun()>0 ;
    }
}
