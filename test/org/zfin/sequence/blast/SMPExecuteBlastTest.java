package org.zfin.sequence.blast;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.BlastResultBean;
import org.zfin.sequence.blast.results.view.BlastResultMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * This tests handles the ExecuteBlast code.
 */
public class SMPExecuteBlastTest {

    private final Logger logger = Logger.getLogger(SMPExecuteBlastTest.class) ;


    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }


    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
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
    @Test
    public void blastOneDBToStream(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        xmlBlastBean.setProgram("blastn");
        File file = new File("test/pax6a-004.fa") ;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) ;
            StringBuilder sb = new StringBuilder() ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                sb.append(line).append("\n") ;
            }
            xmlBlastBean.setQuerySequence(sb.toString());
            xmlBlastBean.setNumChunks(4);

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION) ;
            actualDatabases.add(blastDatabase) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);


            // set filter
            xmlBlastBean.setSeg(true);
            xmlBlastBean.setXnu(true);

            // set word length
            xmlBlastBean.setWordLength(12);

            // set matrix, we only provide support for protein matrices, currently
//            xmlBlastBean.setMatrix(XMLBlastBean.Matrix.PAM30.toString());


            String returnXML = SMPWublastService.getInstance().robustlyBlastOneDBToString(xmlBlastBean) ;
            logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());


            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new ByteArrayInputStream(returnXML.getBytes()));
            assertNotNull(blastOutput);
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;
            assertNotNull(blastResultBean);
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());  //To change body of catch statement use File | Settings | File Templates.
        }
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
    @Test
    public void blastMultipleDBsToStream(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        xmlBlastBean.setProgram("blastn");
        File file = new File("test/pax6a-004.fa") ;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) ;
            StringBuilder sb = new StringBuilder() ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                sb.append(line).append("\n") ;
            }
            xmlBlastBean.setQuerySequence(sb.toString());
            xmlBlastBean.setNumChunks(4);

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase1 = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION) ;
            actualDatabases.add(blastDatabase1) ;
            Database blastDatabase2 = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.GENOMICDNA) ;
            actualDatabases.add(blastDatabase2) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);


            // set filter
            xmlBlastBean.setSeg(true);
            xmlBlastBean.setXnu(true);

            // set word length
            xmlBlastBean.setWordLength(12);

            // set matrix, we only provide support for protein matrices, currently
//            xmlBlastBean.setMatrix(XMLBlastBean.Matrix.PAM30.toString());


            String returnXML = SMPWublastService.getInstance().robustlyBlastOneDBToString(xmlBlastBean) ;
            logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());


            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new ByteArrayInputStream(returnXML.getBytes()));
            assertNotNull(blastOutput);
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;
            assertNotNull(blastResultBean);
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());  //To change body of catch statement use File | Settings | File Templates.
        }
    }



    /**
     * This test should mimic the following
     * wu-blastall -p blastn
     * -d /Users/ndunn/BLASTFILES/zfin_gb_seq
     * -e 1.0E-25
     * -m 7
     * -F F  < ~/svn/ZFIN_WWW/test/pax6a-004.fa
     */
    @Test
    public void smpBlastQueryThread(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        xmlBlastBean.setProgram("blastn");
        File file = new File("test/pax6a-004.fa") ;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) ;
            StringBuilder sb = new StringBuilder() ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                sb.append(line).append("\n") ;
            }
            xmlBlastBean.setQuerySequence(sb.toString());
            xmlBlastBean.setNumChunks(4);

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase1 = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION) ;
            actualDatabases.add(blastDatabase1) ;
            Database blastDatabase2 = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.GENOMICDNA) ;
            actualDatabases.add(blastDatabase2) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);


            // set filter
            xmlBlastBean.setSeg(true);
            xmlBlastBean.setXnu(true);

            // set word length
            xmlBlastBean.setWordLength(12);

            // set matrix, we only provide support for protein matrices, currently
//            xmlBlastBean.setMatrix(XMLBlastBean.Matrix.PAM30.toString());

            File tempFile = File.createTempFile("dump",".fa") ;
            tempFile.deleteOnExit();
            xmlBlastBean.setResultFile(tempFile);

            BlastQueryJob blastQueryJob = new SMPBlastQueryThread(xmlBlastBean,SMPWublastService.getInstance()) ;
            Thread t = new Thread(blastQueryJob) ;
            t.start();
            while(t.isAlive()){
                Thread.sleep(200);
            }

            logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());
//
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(xmlBlastBean.getResultFile()));
            assertNotNull(blastOutput);
            BlastResultBean blastResultBeanUnmarshalled = BlastResultMapper.createBlastResultBean(blastOutput);
            assertNotNull(blastResultBeanUnmarshalled);

        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private class ScheduleBlastOneDBToString extends Thread {

        private XMLBlastBean xmlBlastBean ;

        public ScheduleBlastOneDBToString(XMLBlastBean xmlBlastBean) {
            this.xmlBlastBean = xmlBlastBean ;
        }


        public void run(){
            xmlBlastBean.setNumChunks(4);
            BlastQueryJob blastSingleTicketQueryThread = new SMPBlastQueryThread(xmlBlastBean,SMPWublastService.getInstance());
            BlastQueryThreadCollection.getInstance().addJobAndStart(blastSingleTicketQueryThread);
        }
    }

    private boolean isScheduledRunning(List<ScheduleBlastOneDBToString> rawBlastOneDBToStringList){
        for(ScheduleBlastOneDBToString thread : rawBlastOneDBToStringList){
            if(thread.isAlive()){
                return true ;
            }
        }
        return false ;
    }

    @Test
    public void scheduleBlast(){
        try {
            List<ScheduleBlastOneDBToString> scheduledBlastOneDBToStringList = new ArrayList<ScheduleBlastOneDBToString>() ;
            int numThreads = 10 ;

            // need to number each one one differently

            XMLBlastBean xmlBlastBean = new XMLBlastBean();
            xmlBlastBean.setProgram(XMLBlastBean.Program.BLASTN.getValue());
            File file = new File("test/pax6a-004.fa") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) ;
            StringBuilder sb = new StringBuilder() ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                sb.append(line).append("\n") ;
            }
            xmlBlastBean.setQuerySequence(sb.toString());
            xmlBlastBean.setNumChunks(4);

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION) ;
            actualDatabases.add(blastDatabase) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);


            // set filter
            xmlBlastBean.setSeg(true);
            xmlBlastBean.setXnu(true);

            // set word length
            xmlBlastBean.setWordLength(12);


            for(int i = 0 ; i < numThreads ; i++){
                XMLBlastBean blastBean = xmlBlastBean.clone();
                blastBean.setTicketNumber((new Integer(i)).toString());
                scheduledBlastOneDBToStringList.add(new ScheduleBlastOneDBToString(blastBean)) ;
            }

            for(ScheduleBlastOneDBToString scheduledBlastOneDBToString : scheduledBlastOneDBToStringList){
//            scheduledBlastOneDBToString.start();
                scheduledBlastOneDBToString.run();
            }


            while(isScheduledRunning(scheduledBlastOneDBToStringList)){
                Thread.sleep(1000);
            }

            Thread.sleep(1000);

            while(BlastThreadService.isQueueActive(BlastQueryThreadCollection.getInstance())){
                Thread.sleep(1000);
                logger.info(BlastThreadService.getRunningThreadCount(BlastQueryThreadCollection.getInstance())) ;
            }

            Thread.sleep(1000);

            for(int i = 0 ; i < numThreads ; i++){
                String ticketNumber = (new Integer(i)).toString();
                xmlBlastBean.setTicketNumber(ticketNumber);
                File resultFile = xmlBlastBean.getResultFile();
                JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
                Unmarshaller u = jc.createUnmarshaller();
                BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(resultFile));
                assertNotNull(blastOutput) ;
                BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;
                assertNotNull(blastResultBean);
            }
            logger.info("XMLBlastBean resultFile: " + xmlBlastBean.getResultFile());
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());
        }



    }



}