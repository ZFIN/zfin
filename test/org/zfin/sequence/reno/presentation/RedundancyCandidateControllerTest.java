package org.zfin.sequence.reno.presentation;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.framework.HibernateUtil.getSessionFactory;
import org.zfin.infrastructure.DataNote;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.RenoTestData;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.Set;


/**
 * Tests the RedundancyCandidateController methods for redundancy runs.
 */
public class RedundancyCandidateControllerTest {

    private final RenoTestData renoTestData = new RenoTestData() ;

    private static Logger logger = Logger.getLogger(RedundancyCandidateControllerTest.class);
    private static RenoRepository renoRepository = RepositoryFactory.getRenoRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();


    //these are strings that are used more then one test, and don't get created in the createRenoData() data method.
    private final static String BASIC_NOTE = "This is a basic note";
    private final static String RENAME = "stest";


    static {
        SessionFactory sessionFactory = getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
        //TestConfiguration.unsetAuthenticatedUser();
    }

    /**
     * A note is submitted via clicking Done (excluding Problem/Ignore case) for a Redundancy Run.
     * Selected a gene from the pulldown list (not novel).
     * Note is saved on marker data note and then cleared from the
     * candidate.
     */
   @Test
    public void moveNoteToGeneForRedundancy() {

        Session session = currentSession();
        session.beginTransaction();

        try {
            CandidateBean candidateBeanRedun = setUpBasicBeanRedunDoneSubmission();
            RedundancyCandidateController redundancyCandidateController = new RedundancyCandidateController();
            RunCandidate runCandidate = candidateBeanRedun.getRunCandidate();

            //assume the curator picks the best hit (aka the first existing gene from the pulldown that is not novel).
//            Marker geneFromSelectList = null ;
            
            redundancyCandidateController.handleDone(candidateBeanRedun);

            //I want to only test the handlNote method here, but the handleNote method
            //is private; so I need to call public method like handleDone() to get handleNote()
            //to be called.  But, I can only assert things about the note and perhaps that's all that is required?

            //check that the note has been added to data_note.
            Marker renoGene = markerRepository.getMarkerByAbbreviation("reno");
            Set<DataNote> dNotes = renoGene.getDataNotes();

            logger.info("thedNote set is: " + dNotes);
            assertEquals("the size of the dataNote set is 1", dNotes.size(), 1);
            logger.debug("the note is: " + BASIC_NOTE);

            String testNote = null;
            DataNote dNoteFound = null;
            for (DataNote dnote : dNotes) {
                logger.debug("the note is: " + dnote.getNote());
                if (dnote.getNote().equals(BASIC_NOTE)) {
                    testNote = dnote.getNote();
                    dNoteFound = dnote;
                    logger.debug("we found the test note" + testNote);
                }
            }
            //make sure the note shows up.
            assertEquals("test note exists", BASIC_NOTE, testNote);

            //make sure the candidateBean is DONE or SAVE_NOTE.
            assertTrue("the candidateBean action is either DONE",
                    candidateBeanRedun.getAction().equals((CandidateBean.DONE)) ||
                            candidateBeanRedun.getAction().equals((CandidateBean.SAVE_NOTE)));

            //make sure the dataNote got added to the right gene.
            assertTrue("the dateNote data_id is the renoGene", dNoteFound.getDataZdbID().equals(renoGene.getZdbID()));
            //make sure the candidate note is null post moveNoteToGene.
            assertNull(runCandidate.getCandidate().getNote());
        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }



    @Test
    public void associateWithExistingGeneNoRename() {
        Session session = currentSession();
        session.beginTransaction() ; 
        logger.debug("Enter associateWithExistingGeneNoRename()");


        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();        
            RedundancyCandidateController candidateController = new RedundancyCandidateController();

            //assume the curator picks the best hit (aka the first existing gene from the pulldown that is not novel).
            Marker geneFromSelectList = markerRepository.getMarkerByAbbreviation("reno");
            assertEquals("Gene from select list has same abbreviation", geneFromSelectList, markerRepository.getMarkerByAbbreviation("reno"));
            logger.info("load up the candidateBean with associatedWithExistingGene fake data");

            candidateBean.setAssociatedGeneField(geneFromSelectList.getZdbID());
            logger.info("associatedGEneField abbrev: " + geneFromSelectList.getAbbreviation());
            logger.info("tell the candidateController to handleDone");
            candidateController.handleDone(candidateBean);
            ///Here is the start of the test that should be true
            //test gene should get a mrel to test cdna and mrel should get an attribution
            //also a marker_history record should have been created and the reason changed.

            Marker renoGene = markerRepository.getMarkerByAbbreviation("reno");
            logger.info("renoGene:" + renoGene.toString());

            Marker renoCdna = markerRepository.getMarkerByAbbreviation("MGC:test");
            logger.info("renoCdna:" + renoCdna.toString());

            MarkerRelationship renoMrel = markerRepository.getMarkerRelationship(
                    renoGene,
                    renoCdna,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

            //the gene has a relationship to the cdna
            assertNotNull("renoMrel not null", renoMrel);

            logger.info("mrelZdbId: " + renoMrel.getZdbID());

            RecordAttribution renoMrelAttribution = infrastructureRepository.getRecordAttribution(
                    renoMrel.getZdbID(),
                    ((RedundancyRun) candidateBean.getRunCandidate().getRun()).getRelationPublication().getZdbID(), null);

            String testData = renoMrelAttribution.getDataZdbID();
            String testAttrib = renoMrelAttribution.getSourceZdbID();
            String testType = renoMrelAttribution.getSourceType().toString();
            logger.debug("renoMrelAttribution: "+renoMrelAttribution) ;
            
            assertEquals("recattrib source id is the run attrib", candidateBean.getNomenclaturePublicationZdbID(), testAttrib);
            assertEquals("recattrib data id is the mrel id", renoMrel.getZdbID(), testData);
            assertEquals("recattrib type is standard", testType, "standard");

        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }


    @Test
    public void associateWithExistingGeneWithRename() {
        Session session = currentSession();
        session.beginTransaction() ; 



        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RedundancyCandidateController candidateController = new RedundancyCandidateController();

            //assume the curator picks the best hit (aka the first existing gene from the pulldown that is not novel).
            Marker geneFromSelectList = markerRepository.getMarkerByAbbreviation("reno");
            assertEquals("Gene from select list has same abbreviation", geneFromSelectList, markerRepository.getMarkerByAbbreviation("reno"));
            candidateBean.setAssociatedGeneField(geneFromSelectList.getZdbID());
            //fill the bean with the selected runCandidate
            logger.info("load the runCandidate into candidateBean: RenoControllorTest.associateWithExistingGene;");

            //set the associatedMarker here instead of in the insert so we can keep the
            //insert more generic and usable in cases where we handle null associated markers.
//            runCandidate.getCandidate().setIdentifiedMarker(markerRepository.getMarkerByAbbreviation("MGC:test"));

            logger.info("load up the candidateBean with associatedWithExistingGene fake data");
            //   candidateBean.setLock(true);

            candidateBean.setRename(true);

            //candidateBean.setAction(CandidateBean.DONE);
            logger.info("rename the candidatebean");
            candidateBean.setGeneAbbreviation(RENAME);

            //tell the controller to execute handle done with the bean that was created here.
            logger.info("tell the candidateController to handleDone");
            candidateController.handleDone(candidateBean);
            currentSession().flush();

            ///Here is the start of the test that should be true
            //test gene should get a mrel to test cdna and mrel should get an attribution
            //also a marker_history record should have been created and the reason changed.
            //the rename boolean has been set, as has the geneAbbreviation field, so we
            //should've renamed the gene from "reno" in the test data insert, to
            //"stest" in the candidateBean.

            Marker renoGene = markerRepository.getMarkerByAbbreviation(RENAME);
            Marker renoCdna = markerRepository.getMarkerByAbbreviation("MGC:test");

            //        MarkerRelationship mrelDebug = markerRepository.getMarkerRelationshipByID("ZDB-MREL-070919-1");

            Set<MarkerRelationship> mrelTest = renoGene.getFirstMarkerRelationships();
            logger.info("mrelTEst set is not null? " + mrelTest);

            logger.info("renoGene:" + renoGene.toString());
            logger.info("renoCdna:" + renoCdna.toString());

            //   logger.info("mrelDetails:" + mrelDebug.getFirstMarker().getAbbreviation() + mrelDebug.getSecondMarker().getAbbreviation());

            MarkerRelationship renoMrel = markerRepository.getMarkerRelationship(
                    renoGene,
                    renoCdna,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

            logger.info("mrelZdbId: " + renoMrel.getZdbID());

            //the gene has a relationship to the cdna
            assertNotNull("renoMrel not null", renoMrel);

            RecordAttribution renoMrelAttribution = infrastructureRepository.getRecordAttribution(
                    renoMrel.getZdbID(),
                    candidateBean.getNomenclaturePublicationZdbID(), null);

            String testData = renoMrelAttribution.getDataZdbID();
            String testAttrib = renoMrelAttribution.getSourceZdbID();
            String testType = renoMrelAttribution.getSourceType().toString();

            assertEquals("recattrib source id is the run attrib", candidateBean.getNomenclaturePublicationZdbID(), testAttrib);
            assertEquals("recattrib data id is the mrel id", renoMrel.getZdbID(), testData);
            assertEquals("recattrib type is standard", testType, "standard");

            //this is the difference between existing gene with rename and
            //existing gene w/o rename
            MarkerHistory mhist = markerRepository.getLastMarkerHistory(renoGene, MarkerHistory.Event.REASSIGNED);

            assertEquals("mhist reason is:", MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES, mhist.getReason());

            /*  RecordAttribution mhistRecattribData =
                     infrastructureRepository.getRecordAttribution(mhist.getZdbID(),
                                                                    run.getEntity().getZdbID(),
                                                                    null);
            //we should have a record_attribution record for the name change.
            assertNotNull("record attribution for mhist record exists",mhistRecattribData);
            */

        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }

    @Test
    public void associateNovelGeneWithIdentifiedMarker() {
        Session session = currentSession();
        session.beginTransaction() ; 



        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RedundancyCandidateController candidateController = new RedundancyCandidateController();
            //set the the runCandidate to be the one made from the createRenoData method
            //this is fake data.

            //assume the curator picks NOVEL.

            logger.info("load up the candidateBean with associatedWithNovelGene fake data");

            candidateBean.setAssociatedGeneField(CandidateBean.NOVEL);

            candidateBean.setRename(false);

            logger.info("tell the candidateController to handleDone");

            candidateController.handleDone(candidateBean);

            ///Here is the start of the test that should be true
            //gene should be created
            //mrel should be created (as idendified marker is present on our test candidate
            //mrel attribution should be created
            //mhist should be created
            //mhist reason should be changed

            Marker renoGene = markerRepository.getMarkerByAbbreviation("zgc:test");
            Marker renoCdna = markerRepository.getMarkerByAbbreviation("MGC:test");

            logger.info("novelgene zdb_id: " + renoGene.getZdbID());
            assertNotNull("renoGene", renoGene);

            MarkerRelationship renoMrel = markerRepository.getMarkerRelationship(
                    renoGene,
                    renoCdna,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
            logger.info("renoMrel: " + renoMrel) ; 
            //the gene has a relationship to the cdna
            assertNotNull("renoMrel not null",renoMrel);

            logger.debug("mrel zdb_id: " + renoMrel.getZdbID());

/*

RecordAttribution renoMrelAttribution = infrastructureRepository.getRecordAttribution(
    renoMrel.getZdbID(),
    run.getEntity().getZdbID(),
    RecordAttribution.SourceType.STANDARD);

//the mrel has an attribution
assertNotNull("attribution is created to mrel",renoMrelAttribution);*/

            MarkerHistory mhist = markerRepository.getLastMarkerHistory(renoGene, null);

            //the renoGene has a marker_history record, this basically tests the trigger is working.
            assertNotNull("renoGene has marker_history record", mhist);
            assertTrue(mhist.getReason().equals(MarkerHistory.Reason.NOT_SPECIFIED));

        }

        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }

    @Test
    public void associateWithNovelGeneNoIdentifiedMarker() {
        Session session = currentSession();
        session.beginTransaction() ; 



        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RedundancyCandidateController candidateController = new RedundancyCandidateController();
            //set the the runCandidate to be the one made from the createRenoData method
            //this is fake data.

            //assume the curator picks NOVEL.
            //no associated marker has been identified.                

            logger.info("load up the candidateBean");
            candidateBean.setAssociatedGeneField(CandidateBean.NOVEL);
//            runCandidate.getCandidate().setIdentifiedMarker(null);
            candidateBean.setRename(false);

            logger.info("tell the candidateController to handleDone");
            //tell the controller to execute handle done with the bean that was created here.
            candidateController.handleDone(candidateBean);
            currentSession().flush();
            ///Here is the start of the test that should be true
            //gene should be created
            //dblinks should be added to novel gene
            //mhist should be created
            //mhist reason should be changed

            Marker renoGene = markerRepository.getMarkerByAbbreviation("zgc:test");
            logger.info("novelgene zdb_id: " + renoGene.getZdbID());
            assertNotNull(renoGene);

            //Marker renoCdna = markerRepository.getMarkerByAbbreviation("MGC:test");

            MarkerHistory mhist = markerRepository.getLastMarkerHistory(renoGene, null);

            logger.info("mhist " + mhist.toString());
            //the renoGene has a marker_history record
            assertNotNull(mhist);

            //the mhist record has a reno reason
            // assertTrue(mhist.getReason().equals(MarkerHistory.RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE));
            assertEquals("Marker history has same reason", mhist.getReason(), (MarkerHistory.Reason.NOT_SPECIFIED));

            //the dblinks have been created; at least there is the same number.
            //
            //Note:  this is ture, but I would check this with a query.  
            //The way that marker.getDbLinks is setup it won't be hydrated automatically this way.
            //To fix this would have to setup an inverse relationship.  
            //It is only used in HibernateMarkerRepository, but it is used in a safe way there.
//
//            Set<MarkerDBLink> dbLinks = renoGene.getDbLinks();
//            logger.debug("dblinks is : " + dbLinks);
//            //Set<MarkerDBLink> relatedDbLinks ;
//
//            int numberOfDbLinks = 0;
//
//            for (MarkerDBLink link : dbLinks) {
//                for (Query query : runCandidate.getCandidateQueries()) {
//                    //only get the accessions that have been added for this candidate
//                    if ((query.getAccession().getNumber().equals(link.getAccessionNumber())) &&
//                            (query.getAccession().getReferenceDatabase().equals(link.getReferenceDatabase()))) {
//                        numberOfDbLinks++;
//                    }
//                }
//
//            }
//            int numberOfQueries = runCandidate.getCandidateQueries().size();
//            assertTrue(numberOfQueries == numberOfDbLinks);

        }

        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }


    @Test 
    public void testProblemFlag() {
        Session session = currentSession();
        session.beginTransaction() ; 



        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RunCandidate runCandidate = candidateBean.getRunCandidate();
            RedundancyCandidateController candidateController = new RedundancyCandidateController();
            //set the the runCandidate to be the one made from the createRenoData method
            //this is fake data.
            //assume the curator picks PROBLEM.

            logger.info("load up the candidateBean");
            candidateBean.setCandidateProblem(true);
            candidateBean.setAction(CandidateBean.SET_PROBLEM);
            logger.info("tell the candidateController to handleView");
            //tell the controller to execute handle done with the bean that was created here.
            candidateController.handleView(candidateBean);
            currentSession().flush();

            ///Here is the start of the test that should be true
            //problem and done flags in candidate are set, and that is it.

            assertFalse("Not a rename",candidateBean.isRename());
            logger.info("the.isProblem flag is:" + runCandidate.getCandidate().isProblem());

            assertTrue(runCandidate.getCandidate().isProblem());
            assertFalse(runCandidate.isDone());
            assertNotNull(runCandidate.getCandidate().getNote());
        }

        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }

    @Test 
    public void testIgnoreFlag() {
        Session session = currentSession();
        session.beginTransaction() ; 



        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RunCandidate runCandidate = candidateBean.getRunCandidate();
            RedundancyCandidateController candidateController = new RedundancyCandidateController();
            //set the the runCandidate to be the one made from the createRenoData method
            //this is fake data.

            logger.info("load up the candidateBean");
            candidateBean.setAssociatedGeneField(CandidateBean.IGNORE);


            logger.info("tell the candidateController to handleDone");
            //tell the controller to execute handle done with the bean that was created here.
            candidateController.handleDone(candidateBean);

            ///Here is the start of the test that should be true
            //done flag in candidate is set, and that is it.
            assertTrue(runCandidate.isDone());

        }

        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }

    @Test
    public void testZdbIdFieldNotNull() {
        Session session = currentSession();
        session.beginTransaction() ; 

        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RunCandidate runCandidate = candidateBean.getRunCandidate();
            RedundancyCandidateController redundancyCandidateController = new RedundancyCandidateController();
            //set the the runCandidate to be the one made from the createRenoData method
            //this is fake data.

            logger.info("load up the candidateBean");
            //this is fgf8a; I guess I could make another fake one too...
            candidateBean.setGeneZdbID("ZDB-GENE-990415-72");
            //have to set the associatedGeneField to something, the code takes the
            //geneZdbId as precedence, but the associatedGeneField has to have a value.
            candidateBean.setAssociatedGeneField(CandidateBean.NOVEL);

            logger.info("tell the candidateController to handleDone");
            //tell the controller to execute handle done with the bean that was created here.

            redundancyCandidateController.handleDone(candidateBean);
            currentSession().flush();

            assertTrue(runCandidate.isDone());

            //this is fgf8a
            Marker gene = markerRepository.getMarkerByID("ZDB-GENE-990415-72");
            Marker cDNA = markerRepository.getMarkerByAbbreviation("MGC:test");

            MarkerRelationship renoMrel = markerRepository.getMarkerRelationship(
                    gene,
                    cDNA,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

            logger.debug("mrel zdb_id: " + renoMrel);

            assertNotNull("mrel exists between gene and cdna", renoMrel);
            //the gene has a relationship to the cdna

/*            assertNotNull("renoMrel not null",renoMrel);

            RecordAttribution renoMrelAttribution = infrastructureRepository.getRecordAttribution(
                    renoMrel.getZdbID(),
                    run.getEntity().getZdbID(),
                    RecordAttribution.SourceType.STANDARD);

            //the mrel has an attribution
            assertNotNull("attribution is created to mrel",renoMrelAttribution);*/
        }

        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }

    @Test 
    public void testRedunBeanSetUp() {
        Session session = currentSession();
        session.beginTransaction() ; 


        try {
            CandidateBean candidateBean = setUpBasicBeanRedunDoneSubmission();
            RunCandidate runCandidate = candidateBean.getRunCandidate();
            logger.info("assume we have at least one runCandidate for at least 1 redundancy run" + runCandidate.getZdbID());
            assertNotNull("RunCandidate is not null", runCandidate);
            assertTrue("Run is a redundancy run: ", runCandidate.getRun() instanceof RedundancyRun);
        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }
    }



    /**
     * Basic Candidate Bean object, mimicking a 'Done' submission.
     * Rename checkbox unchecked
     * Basic note
     * @return candidate bean
     */
    private CandidateBean setUpBasicBeanRedunDoneSubmission() {
        renoTestData.setUpSharedRedundancyAndNomenclatureData();
        String redundancyRunCandidateZdbID = renoTestData.createRedundancyData();
        CandidateBean candidateBean = new CandidateBean();
         
        RunCandidate runCandidate = renoRepository.getRunCandidateByID(redundancyRunCandidateZdbID);
        candidateBean.setRunCandidate(runCandidate);
        candidateBean.setGeneZdbID("");
        //could associatedGeneField be refactored to: itemPickedFromSelectList
        candidateBean.setAssociatedGeneField((markerRepository.getMarkerByAbbreviation("reno")).getZdbID());
        candidateBean.setCandidateNote(BASIC_NOTE);
        //assume that they don't want to rename for Redun, then set explicitly to true in the
        //casses where they do want to rename.
        candidateBean.setRename(false);
        candidateBean.setAction(CandidateBean.DONE);
        candidateBean.setNomenclaturePublicationZdbID( ((RedundancyRun) candidateBean.getRunCandidate().getRun()).getRelationPublication().getZdbID());

        logger.info(candidateBean.getAction());
        return candidateBean;
    }



}
