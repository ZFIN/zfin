package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This tests handles the ExecuteBlast code.
 */
public class BlastStressLargeDBTest extends BlastStressTest{

    private final Logger logger = Logger.getLogger(BlastStressLargeDBTest.class) ;

    @Before
    public void setUp() {
        setAbbrev(Database.AvailableAbbrev.VEGA_ZFIN);
        super.setUp();
    }

    /**
     * This test should mimic the following
     * ssh ndunn@genomix.cs.uoregon.edu
     * wu-blastall -p blastn
     * -d /Users/ndunn/BLASTFILES/zfin_gb_seq
     * -e 1.0E-25
     * -m 7
     * -F F  < ~/svn/ZFIN_WWW/test/pax6a-004.fa
     */
//    @Test
    public void rawBlastOneDBToStream(){
        Vector<String> xmlResults = new Vector<String>() ;
        List<RawBlastOneDBToString> rawBlastOneDBToStringList = new ArrayList<RawBlastOneDBToString>() ;
        int numThreads = 40 ;
        for(int i = 0 ; i < numThreads ; i++){
            rawBlastOneDBToStringList.add(new RawBlastOneDBToString(xmlBlastBean,blastDatabase,xmlResults)) ;
        }

        for(RawBlastOneDBToString rawBlastOneDBToString : rawBlastOneDBToStringList){
            rawBlastOneDBToString.start();
        }

        while(isRawRunning(rawBlastOneDBToStringList)) {
            // puase here I guess
        }

        assertEquals(numThreads,xmlResults.size());

        String xmlResult = xmlResults.get(0) ;
        for(int i = 1 ; i < numThreads ; i++){
            xmlResult.equals(xmlResults.get(i)) ;
        }


        logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());
    }

    @Test
    public void scheduleBlast(){
        List<ScheduleBlastOneDBToString> scheduledBlastOneDBToStringList = new ArrayList<ScheduleBlastOneDBToString>() ;
        int numThreads = 40;

        // need to number each one one differently


        for(int i = 0 ; i < numThreads ; i++){
            XMLBlastBean blastBean = xmlBlastBean.clone();
            blastBean.setTicketNumber((new Integer(i)).toString());
            scheduledBlastOneDBToStringList.add(new ScheduleBlastOneDBToString(blastBean)) ;
        }

        for(ScheduleBlastOneDBToString scheduledBlastOneDBToString : scheduledBlastOneDBToStringList){
            scheduledBlastOneDBToString.start();
        }


        try {
            while(isScheduledRunning(scheduledBlastOneDBToStringList)){
                Thread.sleep(1000);
            }

            Thread.sleep(1000);

            while(BlastSingleQueryThreadCollection.getInstance().isQueueActive()){
                Thread.sleep(1000);
                logger.info(BlastSingleQueryThreadCollection.getInstance().getNumberRunningThreads()) ;
            }

            Thread.sleep(1000);

            for(int i = 0 ; i < numThreads ; i++){
                String ticketNumber = (new Integer(i)).toString();
                xmlBlastBean.setTicketNumber(ticketNumber);
                File resultFile = xmlBlastBean.getResultFile();
                JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
                Unmarshaller u = jc.createUnmarshaller();
                BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(resultFile));
            }
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());
        }


        // check for the unmarshalled result for each, with data
        // assertEquals on the data
//        for(int i = 0 ; i < numThreads ; i++){
//            xmlBlastBean.setTicketNumber((new Integer(i)).toString());
//            scheduledBlastOneDBToStringList.add(new ScheduleBlastOneDBToString(xmlBlastBean)) ;
//        }

        logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());
    }

    private boolean isScheduledRunning(List<ScheduleBlastOneDBToString> rawBlastOneDBToStringList){
        for(ScheduleBlastOneDBToString thread : rawBlastOneDBToStringList){
            if(thread.isAlive()){
                return true ;
            }
        }
        return false ;
    }

    private boolean isRawRunning(List<RawBlastOneDBToString> rawBlastOneDBToStringList){
        for(RawBlastOneDBToString thread : rawBlastOneDBToStringList){
            if(thread.isAlive()){
                return true ;
            }
        }
        return false ;
    }

    private class RawBlastOneDBToString extends Thread{

        private List<String> xmlStrings  ;
        private XMLBlastBean xmlBlastBean ;
        private Database database  ;

        public RawBlastOneDBToString(XMLBlastBean xmlBlastBean,Database blastDatabase,List<String> xmlResults){
            this.xmlStrings = xmlResults ;
            this.xmlBlastBean = xmlBlastBean ;
            this.database = blastDatabase ;
        }

        public void run(){
            try {
                xmlStrings.add(MountedWublastBlastService.getInstance().blastOneDBToString(xmlBlastBean,database)) ;
            } catch (BlastDatabaseException e) {
                logger.error(e.fillInStackTrace());
            }
        }
    }

    private class ScheduleBlastOneDBToString extends Thread {

        private XMLBlastBean xmlBlastBean ;

        public ScheduleBlastOneDBToString(XMLBlastBean xmlBlastBean) {
            this.xmlBlastBean = xmlBlastBean ;
        }


        public void run(){
            BlastSingleQueryThreadCollection.getInstance().executeBlastThread(xmlBlastBean);
        }
    }
}