package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Sequence;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.sequence.blast.results.BlastOutput;
import org.zfin.sequence.blast.results.view.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This tests handles the ExecuteBlast code.
 */
public class ExecuteBlastTest {

    private final Logger logger = Logger.getLogger(ExecuteBlastTest.class) ;


    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }


    @Before
    public void setUp() {
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    public void mapBlastOutput(){

        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            assertNotNull("blast output should not be null",blastOutput) ;
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void renderBlastOutput(){

        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;

            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;

            // look at first hit, an ottdart
            HitViewBean hitViewBean = blastResultBean.getHits().get(0) ;
            assertEquals(1,hitViewBean.getHitNumber());
            assertEquals("OTTDART00000023087",hitViewBean.getAccessionNumber());
            assertEquals("pax6a-004",hitViewBean.getHitMarker().getName());
            assertEquals("pax6a",hitViewBean.getGenes().iterator().next().getAbbreviation());
            assertTrue(hitViewBean.isHasExpression());
            assertTrue(hitViewBean.isHasExpressionImages());
            assertTrue(hitViewBean.isHasGO());
            assertFalse(hitViewBean.isHasPhenotype());
            assertFalse(hitViewBean.isHasPhenotypeImages());

            // find 3rd hit, a genbank hit, but only a transcript association
            // so we do not bind it to anything
            hitViewBean = blastResultBean.getHits().get(2) ;
            assertEquals(3,hitViewBean.getHitNumber());
            assertEquals("X61389",hitViewBean.getAccessionNumber());
            assertEquals("pax6a",hitViewBean.getGenes().iterator().next().getAbbreviation());
            assertNull("Because this is genbank it should be null",hitViewBean.getHitMarker());
            assertEquals(1,hitViewBean.getVersion());
            assertTrue(hitViewBean.isHasExpression());
            assertTrue(hitViewBean.isHasExpressionImages());
            assertTrue(hitViewBean.isHasGO());
            assertFalse(hitViewBean.isHasPhenotype());
            assertFalse(hitViewBean.isHasPhenotypeImages());

            // here there are both a transcript and clone associated, but since
            // it is genbank, we only show the clone
            hitViewBean = blastResultBean.getHits().get(3) ;
            assertEquals(4,hitViewBean.getHitNumber());
            assertEquals("BC066722",hitViewBean.getAccessionNumber());
            assertEquals("MGC:76866",hitViewBean.getHitMarker().getName());
            assertEquals("pax6a",hitViewBean.getGenes().iterator().next().getAbbreviation());
            assertEquals(1,hitViewBean.getVersion());
            assertTrue(hitViewBean.isHasExpression());
            assertTrue(hitViewBean.isHasExpressionImages());
            assertTrue(hitViewBean.isHasGO());
            assertFalse(hitViewBean.isHasPhenotype());
            assertFalse(hitViewBean.isHasPhenotypeImages());

            // here want to test that we get different results for gene pax6b
            hitViewBean = blastResultBean.getHits().get(14) ;
            assertEquals(15,hitViewBean.getHitNumber());
            assertEquals("OTTDART00000022989",hitViewBean.getAccessionNumber());
            assertEquals("pax6b-006",hitViewBean.getHitMarker().getName());
            assertEquals("pax6b",hitViewBean.getGenes().iterator().next().getAbbreviation());
            assertTrue(hitViewBean.isHasExpression());
            assertTrue(hitViewBean.isHasExpressionImages());
            assertTrue(hitViewBean.isHasGO());
            assertTrue(hitViewBean.isHasPhenotype());
            assertTrue(hitViewBean.isHasPhenotypeImages());


            hitViewBean = blastResultBean.getHits().get(16) ;
            assertEquals(17,hitViewBean.getHitNumber());
            assertEquals("BC076068",hitViewBean.getAccessionNumber());
            assertEquals("MGC:92546",hitViewBean.getHitMarker().getName());
            assertEquals("pax6b",hitViewBean.getGenes().iterator().next().getAbbreviation());
            assertEquals(1,hitViewBean.getVersion());
            assertTrue(hitViewBean.isHasExpression());
            assertTrue(hitViewBean.isHasExpressionImages());
            assertTrue(hitViewBean.isHasGO());
            assertTrue(hitViewBean.isHasPhenotype());
            assertTrue(hitViewBean.isHasPhenotypeImages());
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }


    @Test
    public void testView(){

        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;
            HitViewBean hitViewBean = blastResultBean.getHits().get(8) ;

            assertEquals("OTTDART00000023090",hitViewBean.getAccessionNumber());
            assertEquals(9,hitViewBean.getHitNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(3,highScoringPairList.size());
            List<AlignmentLine> alignmentLines =  highScoringPairList.get(0).getView() ;
            assertTrue(alignmentLines.size()>5); // should be 13 or 14 depending on how the sequence changes
            AlignmentLine alignmentLine = alignmentLines.get(0) ;
            assertEquals(HighScoringPair.DISPLAY_LENGTH,alignmentLine.getQueryStrand().length());
            assertEquals(HighScoringPair.DISPLAY_LENGTH,alignmentLine.getMidlineStrand().length());
            assertEquals(HighScoringPair.DISPLAY_LENGTH,alignmentLine.getHitStrand().length());


            hitViewBean = blastResultBean.getHits().get(0) ;
            assertEquals("OTTDART00000023087",hitViewBean.getAccessionNumber());
            alignmentLine =  hitViewBean.getHighScoringPairs().get(0).getView().get(0) ;
            assertEquals(AlignmentLine.PADDING,alignmentLine.getStartHitString().length());
            alignmentLine = alignmentLines.get(2) ;
            assertEquals(AlignmentLine.PADDING,alignmentLine.getStartHitString().length());
            alignmentLine = alignmentLines.get(5) ;
            assertEquals(AlignmentLine.PADDING,alignmentLine.getStartHitString().length());



        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void skipSameAccession(){
        try{
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;

            HitViewBean hitViewBean = blastResultBean.getHits().get(14) ;
            assertEquals("OTTDART00000022989",hitViewBean.getAccessionNumber());
            assertEquals(15,hitViewBean.getHitNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(1,highScoringPairList.size());
            assertEquals(683,hitViewBean.getHitLength()) ;


            // should skip 22989 in favor of 22988
            hitViewBean = blastResultBean.getHits().get(19) ;
            assertEquals("OTTDART00000022988",hitViewBean.getAccessionNumber());
            assertEquals(21,hitViewBean.getHitNumber());
            highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(1,highScoringPairList.size());
            assertEquals(585,hitViewBean.getHitLength()) ;

        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }


    @Test
    public void testNoGene(){

        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;
            HitViewBean hitViewBean = blastResultBean.getHits().get(20) ;

            assertEquals("ABC123",hitViewBean.getAccessionNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(1,highScoringPairList.size());
            assertEquals(0,hitViewBean.getGenes().size()) ;
            assertEquals(394,hitViewBean.getHitLength()) ;


        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }


    @Test
    public void testAccessionOnly(){

        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;
            HitViewBean hitViewBean = blastResultBean.getHits().get(17) ;

            // this used to be a test for accessions withotu db_links, but now this accession has a db_link
            // use SQL to find new ones:
//select
//ab.accbk_acc_num from accession_bank ab
//where not exists
//(
//select 'x' from db_link dbl
//where ab.accbk_acc_num=dbl.dblink_acc_num
//)
            assertEquals(18,hitViewBean.getHitNumber());
            assertEquals("CW672524",hitViewBean.getAccessionNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(1,highScoringPairList.size());
            assertEquals(0,hitViewBean.getGenes().size()) ;
            assertNull(hitViewBean.getHitDBLink());
            assertNotNull(hitViewBean.getZfinAccession());
            assertEquals(1733,hitViewBean.getHitLength()) ;
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void handleReverseSequence(){
        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;
            HitViewBean hitViewBean = blastResultBean.getHits().get(1) ;

            assertEquals(2,hitViewBean.getHitNumber());
            assertEquals("OTTDART00000023008",hitViewBean.getAccessionNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(130,hitViewBean.getHitLength()) ;
            assertEquals(1,highScoringPairList.size());
            HighScoringPair highScoringPair = highScoringPairList.get(0) ;
            assertEquals(200,highScoringPair.getQueryFrom());
            assertEquals(71,highScoringPair.getQueryTo());
            assertEquals(100,highScoringPair.getHitFrom());
            assertEquals(229,highScoringPair.getHitTo());
            List<AlignmentLine> alignmentLines = highScoringPair.getView() ;
            assertEquals(3,alignmentLines.size());
            AlignmentLine alignmentLine1 = alignmentLines.get(0) ;
            assertEquals(200,alignmentLine1.getStartQuery()) ;
            assertEquals(141,alignmentLine1.getStopQuery()) ;
            assertEquals(100,alignmentLine1.getStartHit()) ;
            assertEquals(159,alignmentLine1.getStopHit()) ;
            AlignmentLine alignmentLine2 = alignmentLines.get(1) ;
            assertEquals(140,alignmentLine2.getStartQuery()) ;
            assertEquals(81,alignmentLine2.getStopQuery()) ;
            assertEquals(160,alignmentLine2.getStartHit()) ;
            assertEquals(219,alignmentLine2.getStopHit()) ;
            AlignmentLine alignmentLine3 = alignmentLines.get(2) ;
            assertEquals(80,alignmentLine3.getStartQuery()) ;
            assertEquals(71,alignmentLine3.getStopQuery()) ;
            assertEquals(220,alignmentLine3.getStartHit()) ;
            assertEquals(229,alignmentLine3.getStopHit()) ;
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }

    @Test
    public void testMorpholino(){
        try {
// create a bean from the JAXB
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File blastInputFile = new File ("test/blastResult.xml") ;

            File blastResultFile = File.createTempFile("blast",".xml") ;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(blastInputFile)) ;
            String xmlString = "" ;
            for(String line ; (line = bufferedReader.readLine())!=null ; ){
                xmlString += line ;
            }
            xmlString = MountedWublastBlastService.getInstance().fixBlastXML(xmlString,null) ;

            BufferedWriter writer = new BufferedWriter(new FileWriter(blastResultFile)) ;
            writer.write(xmlString);
            writer.close();

            BlastOutput blastOutput = (BlastOutput) u.unmarshal(new FileInputStream(blastResultFile));
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;


            // look at all hits
            assertEquals("has 21 hits",21,blastResultBean.getHits().size()) ;
            HitViewBean hitViewBean = blastResultBean.getHits().get(15) ;

            assertEquals(16,hitViewBean.getHitNumber());
            assertEquals("ZDB-MRPHLNO-041109-5",hitViewBean.getAccessionNumber());
            List<HighScoringPair> highScoringPairList = hitViewBean.getHighScoringPairs() ;
            assertEquals(1,highScoringPairList.size());
            assertNull(hitViewBean.getHitDBLink()) ;
            assertNull(hitViewBean.getZfinAccession()) ;
            assertNotNull(hitViewBean.getHitMarker()) ;
            assertEquals("fgf8a",hitViewBean.getGene().getAbbreviation());
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString()) ;
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

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.VEGA_ZFIN) ;
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


            String returnXML = MountedWublastBlastService.getInstance().robustlyBlastOneDBToString(xmlBlastBean,blastDatabase) ;
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

    @Test
    public void distributedBlast(){
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

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.VEGA_ZFIN) ;
            actualDatabases.add(blastDatabase) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);
            MountedWublastBlastService.getInstance().setBlastResultFile(xmlBlastBean);

            BlastHeuristicCollection blastHeuristicCollection = (new SettableBlastHeuristicFactory(5,true)).createBlastHeuristics(xmlBlastBean) ;
            BlastDistributableQueryThread blastDistributableQueryThread = new BlastDistributableQueryThread(xmlBlastBean,blastHeuristicCollection) ;
            blastDistributableQueryThread.run();

            XMLBlastBean resultXMLBlastBean = blastDistributableQueryThread.getXmlBlastBean() ;
            assertNotNull(resultXMLBlastBean);
            assertNotNull(resultXMLBlastBean.getBlastOutput());
            assertNotNull(resultXMLBlastBean.getBlastResultBean());
            assertTrue(resultXMLBlastBean.getBlastResultBean().getHits().size()>2);
        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());
        }
    }


    /**
     * todo: should really test remote-only services, should fix when doing wsdl stuff
     */
    @Test
    public void getSequenceFromLocalSource(){
        // get a dblink from a curated database from pax6a
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.CURATED_MIRNA_MATURE,
                ForeignDBDataType.DataType.RNA,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH
        ) ;

        String hql = "" +
                "select l.accessionNumber from DBLink  l " +
                "where l.referenceDatabase.zdbID = :referenceDatabaseZdbID " +
                "" ;
        org.hibernate.Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("referenceDatabaseZdbID",referenceDatabase.getZdbID()) ;
        query.setMaxResults(1) ;
        String accession = query.list().get(0).toString() ;

        List<Sequence> localSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs(accession,referenceDatabase) ;
        assertNotNull("should not be null",localSequences);
        assertTrue("should find a local sequence",localSequences.size()>0);
        List<Sequence> remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("ant dep") ;
        assertTrue("should also find a remote sequence",remoteSequences.size()==0);
    }

    /**
     * todo: should really test remote-only services, should fix when doing wsdl stuff
     */
    @Test
    public void getSequenceFromRemote(){
        // get a dblink from a genbank database
        List<Sequence> badSequences= MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("ABC123123123ABC") ;
        assertTrue("should find NO sequences", CollectionUtils.isEmpty(badSequences));
        List<Sequence> remoteSequences  ;
        // RNA sequences
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("BC066722") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("NM_131304") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("BQ826503") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("AL929172") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("AY627769") ;
        assertTrue("should find at least one sequence",remoteSequences.size()==1);
        assertTrue("should be a big sequence",remoteSequences.get(0).getData().length()>1000);

        // protein sequences
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("NP_571379") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("ZZZZZZ") ;
        assertTrue(CollectionUtils.isEmpty(remoteSequences));
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("P26630") ;
        assertTrue(remoteSequences.size()>0);
        remoteSequences = MultipleBlastServerService.getSequencesForAccessionAndReferenceDBs("CAI20610") ;
        assertTrue(remoteSequences.size()>0);

        logger.debug(remoteSequences.get(0).getData());
    }

    @Test
    public void productionBlastHeuristic(){
        XMLBlastBean xmlBlastBean = new XMLBlastBean() ;
        List<Database> actualDatabases = new ArrayList<Database>() ;
        // size of the database should be about 191333
        Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.GBK_ZF_DNA) ;
        DatabaseStatistics databaseStatistics ;
        int numSequences = -1 ;
        try {
            databaseStatistics = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(blastDatabase) ;
            numSequences = databaseStatistics.getNumSequences();
            assertNotNull(databaseStatistics.getCreationDate()) ;
            assertNotNull(databaseStatistics.getModifiedDate()) ;
        } catch (Exception e) {
            fail(e.toString()) ;
        }

        actualDatabases.add(blastDatabase) ;
        xmlBlastBean.setActualDatabaseTargets(actualDatabases);
        xmlBlastBean.setExpectValue(1.0E-25);

        ProductionBlastHeuristicFactory productionBlastHeuristicFactory = new ProductionBlastHeuristicFactory() ;
        productionBlastHeuristicFactory.setIntercept( 0 );
        BlastHeuristicCollection blastHeuristicCollection ;
        BlastHeuristic blastHeuristic ;

        productionBlastHeuristicFactory.setSplitSequenceValue(10);
        blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        assertEquals(1,blastHeuristicCollection.getBlastHeuristics().size()) ;
        blastHeuristic = blastHeuristicCollection.getBlastHeuristics().get(0) ;
        assertEquals(blastHeuristic.getDbSplits(),blastHeuristicCollection.getNumChunksForDatabase(blastDatabase)) ;
        assertEquals(productionBlastHeuristicFactory.getMaxSegs(),blastHeuristic.getDbSplits());


        productionBlastHeuristicFactory.setSplitSequenceValue(Integer.MAX_VALUE);
        blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        assertEquals(1,blastHeuristicCollection.getBlastHeuristics().size()) ;
        blastHeuristic = blastHeuristicCollection.getBlastHeuristics().get(0) ;
        assertEquals(blastHeuristic.getDbSplits(),blastHeuristicCollection.getNumChunksForDatabase(blastDatabase)) ;
        assertEquals(10,blastHeuristic.getDbSplits());

        // have to add a little fudge for rounding
        productionBlastHeuristicFactory.setSplitSequenceValue( (float) (2.0/Math.log( (double) numSequences ) ) + 0.0001f );
        blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        assertEquals(1,blastHeuristicCollection.getBlastHeuristics().size()) ;
        blastHeuristic = blastHeuristicCollection.getBlastHeuristics().get(0) ;
        assertEquals(blastHeuristic.getDbSplits(),blastHeuristicCollection.getNumChunksForDatabase(blastDatabase)) ;
        assertEquals(2,blastHeuristic.getDbSplits());

        // have to add a little fudge for rounding
        productionBlastHeuristicFactory.setSplitSequenceValue( (float) (5.0/Math.log( (double) numSequences )) +0.0001f);
        blastHeuristicCollection = productionBlastHeuristicFactory.createBlastHeuristics(xmlBlastBean) ;
        assertEquals(1,blastHeuristicCollection.getBlastHeuristics().size()) ;
        blastHeuristic = blastHeuristicCollection.getBlastHeuristics().get(0) ;
        assertEquals(blastHeuristic.getDbSplits(),blastHeuristicCollection.getNumChunksForDatabase(blastDatabase)) ;
        assertEquals(5,blastHeuristic.getDbSplits());

    }

    @Test
    public void mergeHits(){
        try {
            JAXBContext jc = JAXBContext.newInstance("org.zfin.sequence.blast.results");
            logger.info("jc: "+ jc);
            Unmarshaller u = jc.createUnmarshaller();
            logger.info("u: "+ u);

            File hitAFile = new File ("test/blastAHits.xml") ;
            File hitBFile = new File ("test/blastBNoHits.xml") ;

            BlastOutput blastOutputA = (BlastOutput) u.unmarshal(new FileInputStream(hitAFile));
            BlastOutput blastOutputB = (BlastOutput) u.unmarshal(new FileInputStream(hitBFile));

            BlastResultBean blastResultBeanA = BlastResultMapper.createBlastResultBean(blastOutputA) ;
            BlastResultBean blastResultBeanB = BlastResultMapper.createBlastResultBean(blastOutputB) ;
            assertEquals(22,blastResultBeanA.getHits().size());
            assertEquals(0 ,blastResultBeanB.getHits().size());


            BlastOutput blastOutput = BlastOutputMerger.mergeBlastHits(blastOutputB,blastOutputA) ;
            BlastResultBean blastResultBean = BlastResultMapper.createBlastResultBean(blastOutput) ;

            assertEquals(22,blastResultBean.getHits().size());

            assertEquals(22,blastResultBeanA.getHits().size());
            assertEquals(0 ,blastResultBeanB.getHits().size());

        } catch (Exception e) {
            fail(e.toString()) ;
        }
    }


    /**
     *  Compare wu-blast single, blastn single, blastn distributed
     */
    @Test
    public void testIsSame(){
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

            List<Database> actualDatabases = new ArrayList<Database>() ;
            Database blastDatabase = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.VEGA_ZFIN) ;
            actualDatabases.add(blastDatabase) ;
            xmlBlastBean.setActualDatabaseTargets(actualDatabases);
            xmlBlastBean.setExpectValue(1.0E-25);
            MountedWublastBlastService.getInstance().setBlastResultFile(xmlBlastBean);


            BlastHeuristicCollection blastHeuristicCollectionSingle = (new SettableBlastHeuristicFactory(1,false)).createBlastHeuristics(xmlBlastBean) ;
//            BlastHeuristicFactory blastHeuristicFactory = new ProductionBlastHeuristicFactory() ;
            BlastDistributableQueryThread blastSingleQueryThread = new BlastDistributableQueryThread(xmlBlastBean, blastHeuristicCollectionSingle) ;
            Thread t = new Thread(blastSingleQueryThread);
            t.start();
            while(t.isAlive()){
                Thread.sleep(200);
            }
            XMLBlastBean resultSingleXMLBlastBean = blastSingleQueryThread.getXmlBlastBean() ;
            assertNotNull(resultSingleXMLBlastBean);
            assertNotNull(resultSingleXMLBlastBean.getBlastOutput());
            assertNotNull(resultSingleXMLBlastBean.getBlastResultBean());
            assertTrue(resultSingleXMLBlastBean.getBlastResultBean().getHits().size()>2);

            BlastHeuristicCollection blastHeuristicCollection = (new SettableBlastHeuristicFactory(5,true)).createBlastHeuristics(xmlBlastBean) ;
            BlastDistributableQueryThread blastDistributableQueryThread = new BlastDistributableQueryThread(xmlBlastBean,blastHeuristicCollection) ;
            blastDistributableQueryThread.run();
            XMLBlastBean resultDistributedXMLBlastBean = blastDistributableQueryThread.getXmlBlastBean() ;
            assertNotNull(resultDistributedXMLBlastBean);
            assertNotNull(resultDistributedXMLBlastBean.getBlastOutput());
            assertNotNull(resultDistributedXMLBlastBean.getBlastResultBean());
            assertTrue(resultDistributedXMLBlastBean.getBlastResultBean().getHits().size()>2);


            assertEquals(resultDistributedXMLBlastBean.getBlastResultBean().getHits().size(),resultSingleXMLBlastBean.getBlastResultBean().getHits().size());
            assertEquals(resultDistributedXMLBlastBean.getBlastResultBean(),resultSingleXMLBlastBean.getBlastResultBean());

        } catch (Exception e) {
            fail(e.fillInStackTrace().toString());
        }
    }

    @Test
    public void testSendFASTA(){
        try {
            logger.error("enter: "+ ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH);
            File localFile = File.createTempFile("test",".fa",new File(ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH.value())) ;
            logger.debug("local: " + localFile);
//            localFile.deleteOnExit();
            BufferedWriter writer = new BufferedWriter(new FileWriter(localFile)) ;
            writer.write("some garbage");
            writer.close();
            File remoteFASTAFile = MountedWublastBlastService.getInstance().sendFASTAToServer(localFile,0) ;
            logger.debug("remote: " + remoteFASTAFile);
            assertNotNull(remoteFASTAFile);
            assertEquals(localFile.getName(),remoteFASTAFile.getName());
            assertNotSame(remoteFASTAFile.getAbsolutePath(),localFile.getAbsolutePath());
            // qrsh ls of file

        } catch (IOException e) {
            fail(e.fillInStackTrace().toString()) ;
        }
    }



}
