package org.zfin.sequence.reno.presentation;

import org.zfin.sequence.reno.RenoTestData;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Accession;
import org.zfin.repository.RepositoryFactory;
import static org.zfin.framework.HibernateUtil.getSessionFactory;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.TestConfiguration;
import org.zfin.orthology.Species;
import org.zfin.orthology.OrthoEvidence;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.HashSet;

/**
 * Tests only the nomenclature methods of the CandidateController tests.
 */
public class NomenclatureCandidateControllerTest {

    private RenoTestData renoTestData = new RenoTestData() ;

    private static Logger logger = Logger.getLogger(NomenclatureCandidateControllerTest.class);
    private static RenoRepository renoRepository = RepositoryFactory.getRenoRepository();
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    //these are strings that are used more then one test, and don't get created in the createRenoData() data method.
    private final static String BASIC_NOTE = "This is a basic note";
//    private final static String RENAME = "stest";


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

    @Test
    public void stub(){

    }


    @Ignore
    public void testGeneRename() {
        Session session = currentSession();
        session.beginTransaction() ;


        try {
            CandidateBean candidateBeanNomen = setUpBasicBeanNomen();
            CandidateController candidateController = new CandidateController();

            //tell the candidateController to hadle the done nomenBean
            candidateController.handleDone(candidateBeanNomen);


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

    @Ignore
    public void testNomenBeanSetUp() {
        Session session = currentSession();
        session.beginTransaction() ;


        try {
            CandidateBean candidateBean = setUpBasicBeanNomen();
            RunCandidate runCandidate = candidateBean.getRunCandidate();

            logger.info("assume we have at least one runCandidate for at least 1 nomenclature run" + runCandidate.getZdbID());
            assertNotNull("RunCandidate is not null", runCandidate);
            logger.info("assume we have a nomenclature runType: " + runCandidate.getRun().getClass().getName());
            assertTrue("Candidate is a nomenclature candidate", runCandidate.getRun() instanceof NomenclatureRun);
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

    private CandidateBean setUpBasicBeanNomen() {
        CandidateBean candidateBean = new CandidateBean();
        renoTestData.setUpSharedRedundancyAndNomenclatureData();
        String nomenclatureRunCandidateZdbID = renoTestData.createNomenclatureData();
        RunCandidate runCandidate = renoRepository.getRunCandidateByID(nomenclatureRunCandidateZdbID);

        logger.debug("Load up the bean");
        //set the identified marker by default; most redun data has CDNA cnd_mrkr_zdb_ids
        //set this explicitly further in test cases where it the test wants to see what happens when this is null.
//        runCandidate.getCandidate().setIdentifiedMarker(markerRepository.getMarkerByAbbreviation("reno"));
        candidateBean.setGeneAbbreviation("noment");
        candidateBean.setGeneName("nomen Test 1");

        //have to get the human accession number that the curator would have chosen
        //from the pull down menu.
        //Todo: make this a repository method in the renoRepository?

        ForeignDB foreignDBHuman = sequenceRepository.getForeignDBByName("UniProt");
        ReferenceDatabase refDBHuman = sequenceRepository.getReferenceDatabaseByAlternateKey(
                foreignDBHuman,
                ReferenceDatabase.Type.POLYPEPTIDE,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.HUMAN);
        //copied the acc_num out of the database; maybe this will change??
        Accession humanAccession = sequenceRepository.getAccessionByAlternateKey("AC:NOMENHIT2", refDBHuman);
        //candidateBean.setHumanOrthologueAbbrev(humanAccession);

        Set<OrthoEvidence.Code> orthoEvidencesHuman = new HashSet<OrthoEvidence.Code>();
        orthoEvidencesHuman.add(OrthoEvidence.Code.AA);
        // candidateBean.setHumanOrthologyEvidence(orthoEvidencesHuman);

        //have to get the mouse accession number that the curator would have chosen
        //from the pull down menu.
        //Todo: make this a repository method in the renoRepository?

        ForeignDB foreignDBMouse = sequenceRepository.getForeignDBByName("UniProt");
        ReferenceDatabase refDBMouse = sequenceRepository.getReferenceDatabaseByAlternateKey(
                foreignDBMouse,
                ReferenceDatabase.Type.POLYPEPTIDE,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.MOUSE);
        //copied the acc_num out of the database, maybe this will change?
        Accession mouseAccession = sequenceRepository.getAccessionByAlternateKey("AC:NOMENHIT", refDBMouse);
        //candidateBean.setMouseOrthologueAbbrev(mouseAccession);

        Set<OrthoEvidence.Code> orthoEvidencesMouse = new HashSet<OrthoEvidence.Code>();
        orthoEvidencesMouse.add(OrthoEvidence.Code.NT);
        //candidateBean.setMouseOrthologyEvidence(orthoEvidencesMouse);

        candidateBean.setRunCandidate(runCandidate);
        candidateBean.setGeneZdbID("");

        //set defaults as true, overrride if neccessary in tests.
        candidateBean.setGeneFamilyName("Acyl-CoA synthetases (ACS)");
        candidateBean.setNomenclaturePublicationZdbID(runCandidate.getRun().getNomenclaturePublication().getZdbID());
        candidateBean.setOrthologyPublicationZdbID(  ((NomenclatureRun) runCandidate.getRun()).getOrthologyPublication().getZdbID());
        candidateBean.setCandidateNote(BASIC_NOTE);

        //assume that they want to rename for Nomen, then set explicitly to false in the
        //casses where they do want to rename and just want to add orthology.
        candidateBean.setAssociatedGeneField(null);
        candidateBean.setRename(true);
        candidateBean.setAction(CandidateBean.DONE);
        logger.info(candidateBean.getAction());
        return candidateBean;
    }

}


