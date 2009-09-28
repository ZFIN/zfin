package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastOutputMerger;
import org.zfin.sequence.blast.results.view.BlastResultMapper;
import org.zfin.framework.HibernateUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;


/**
 *  Class BlastDistributedQueryThread is a wrapper for a set of blast operations for a single
 *  query distributed:
 *  1. multiple databases
 *  2. splitting single databases into smaller parts
 */
public class BlastDistributedQueryThread extends BlastSingleQueryThread{

    private final static Logger logger = Logger.getLogger(BlastDistributedQueryThread.class) ;

    private List<BlastSliceThread> blastSlices = new ArrayList<BlastSliceThread>() ;
    private BlastHeuristicCollection blastHeuristicCollection ;

    public BlastDistributedQueryThread(XMLBlastBean xmlBlastBean,BlastHeuristicCollection blastHeuristicCollection){
        super(xmlBlastBean) ;
        this.blastHeuristicCollection = blastHeuristicCollection ;
    }

    // todo: note that we can  not return until we are done running
    public void run(){

        // 1. first split on each database
        // for each database
        List<Database> databases = xmlBlastBean.getActualDatabaseTargets() ;

//         remember we are going to re-assble these later
        for(Database database: databases){
            int numChunks = blastHeuristicCollection.getNumChunksForDatabase(database) ;
            if(numChunks<1){
                numChunks = 1 ;
            }

            // 2. create chunks for each and execute
            for(int i = 0 ; i < numChunks ; i++){
                XMLBlastBean sliceBean = xmlBlastBean.clone();
                sliceBean.setSliceNumber(i);
                sliceBean.setNumChunks(numChunks);

                BlastSliceThread blastSliceThread = new BlastSliceThread(sliceBean,database,i) ;
                blastSlices.add(blastSliceThread) ;
                blastSliceThread.start();
            }
        }


        // sleep a few seconds each time
        while(true==isHasActiveThreads()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }


        BlastOutput blastOutput = BlastOutputMerger.mergeBlastOutput(blastSlices) ;
        xmlBlastBean.setBlastOutput(blastOutput) ;
        xmlBlastBean.setBlastResultBean(BlastResultMapper.createBlastResultBean(blastOutput)) ;

        // the thread is done with the database so close the databse connection
        HibernateUtil.closeSession();


        try {
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            Marshaller u = jc.createMarshaller();
            u.marshal(blastOutput,new BufferedWriter(new FileWriter(xmlBlastBean.getResultFile())));
        } catch (Exception e) {
            logger.fatal("Failed to write blast result to file: " +e);
        }
    }


    public boolean isHasActiveThreads(){
        for(BlastSliceThread blastSliceThread: blastSlices) {
            if(true==blastSliceThread.isAlive()){
                return true ;
            }
        }
        return false ;
    }


}