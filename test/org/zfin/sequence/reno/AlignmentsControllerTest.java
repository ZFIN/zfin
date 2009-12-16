package org.zfin.sequence.reno;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.ModelAndView;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.Species;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Accession;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.reno.presentation.AlignmentsController;
import org.zfin.sequence.reno.presentation.CandidateBean;
import org.zfin.sequence.reno.repository.RenoRepository;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

import static org.junit.Assert.*;

/**
Tests the AlignmentController.
 */
public class AlignmentsControllerTest {

    private static Logger logger = Logger.getLogger(AlignmentsControllerTest.class);
    private static RenoRepository renoRepository = RepositoryFactory.getRenoRepository();
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
    

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }

    }


    @Before
    public void setUp() {
        TestConfiguration.configure();

    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }

        /**
     * To add hits, needs query , needs runcand, needs run, needs publication
     */
    @Test
    public void alignmentsWithDataInsertions() {

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            String runCandidateZdbID = insertRunCandidate();
            logger.debug("inserted runCandidate: " + runCandidateZdbID);
            RunCandidate runCandidate = renoRepository.getRunCandidateByID(runCandidateZdbID);
            assertNotNull("RunCandidate is NOT NULL following insert", runCandidate);
            Run run = runCandidate.getRun();
            assertNotNull("Run is NOT NULL following insert", run);
            Candidate candidate = runCandidate.getCandidate();
            assertNotNull("Candidate is NOT NULL following insert", candidate);

            Set<Query> candidateQueries = runCandidate.getCandidateQueries();
            Query query = candidateQueries.iterator().next();
            assertNotNull("Candidate Queries are NOT NULL following insert", query);

            // assert hits (5)
            List<Hit> hitList = new ArrayList<Hit>(query.getBlastHits());
            assertEquals("Returns 5 hits for this query", 5, hitList.size());
        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            e.printStackTrace();
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
        }


    }


    @Test
    public void alignments() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        ModelAndView mv;

        try {
            String zdbID = insertRunCandidate();
            AlignmentsController controller = new AlignmentsController();
            controller.setSuccessView("alignments-list.page");
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/action/reno/alignments");
            request.setParameter(AlignmentsController.RUNCANDIDATE_ZDBID, zdbID);
            request.setSession(new MockHttpSession(null));
            MockHttpServletResponse response = new MockHttpServletResponse();
            mv = controller.handleRequest(request, response);

            assertEquals("Check success page", "alignments-list.page", controller.getSuccessView());

            CandidateBean formBean;
            Map model = mv.getModel();
            assertNotNull("Returns valid model", model);
            formBean = (CandidateBean) model.get("not a bean string");
            assertNull("Returns no bean for bad string", formBean);
            formBean = (CandidateBean) model.get("formBean");
            assertNotNull("Returns valid formBean", formBean);
            assertEquals("Returns 1 query for this candidate", 1, formBean.getRunCandidate().getCandidateQueries().size());
            Query query = formBean.getRunCandidate().getCandidateQueries().iterator().next();
            assertNotNull("Returns a valid query", query);
            assertEquals("Returns 5 hits for this query", 5, query.getBlastHits().size());
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


//make the new candidate, run, accessions, and run_candidate, returns a runcandidate zdb_id
private String insertRunCandidate() {
    Session session = HibernateUtil.currentSession();

    Marker gene = new Marker();
    gene.setAbbreviation("reno");
    gene.setName("Reno Test Name");
    //should this be an enum?
    gene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
    gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
    session.save(gene);

    Marker cDNA = new Marker();
    cDNA.setAbbreviation("MGC:test");
    cDNA.setName("MGC:test");
    //should this be an enum?
    cDNA.setMarkerType(markerRepository.getMarkerTypeByName("CDNA"));
    cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
    session.save(cDNA);

    PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");

    Accession accession1 = new Accession();
    //accession1.setID((long) 1);
    accession1.setNumber("AC:test1");
    accession1.setDefline("defline jam");
    accession1.setLength(12);
    ReferenceDatabase refDb=sequenceRepository.getReferenceDatabase(
            ForeignDB.AvailableName.GENBANK,
            ForeignDBDataType.DataType.GENOMIC,
            ForeignDBDataType.SuperType.SEQUENCE,
            Species.ZEBRAFISH);
    accession1.setReferenceDatabase(refDb);
    session.save(accession1);
    //logger.info("accession1 number is:"+accession1.getID());

    Accession accession2 = new Accession();
    //accession2.setID((long) 2);
    accession2.setNumber("AC:test2");
    accession2.setDefline("defline jam");
    accession2.setLength(12);
    accession2.setReferenceDatabase(refDb);
    session.save(accession2);
    //logger.info("accession2 number is:" +accession2.getID());

    Accession accession3 = new Accession();
    //accession3.setID((long) 3);
    accession3.setNumber("AC:test3");
    accession3.setDefline("defline jam");
    accession3.setLength(12);
    accession3.setReferenceDatabase(refDb);
    session.save(accession3);

    Accession accession4 = new Accession();
    //accession4.setID((long) 4);
    accession4.setNumber("AC:test4");
    accession4.setDefline("defline jam");
    accession4.setLength(12);
    accession4.setReferenceDatabase(refDb);
    session.save(accession4);

    Accession accession5 = new Accession();
    //accession5.setID((long) 5);
    accession5.setNumber("AC:test5");
    accession5.setDefline("defline jam");
    accession5.setLength(12);
    accession5.setReferenceDatabase(refDb);
    session.save(accession5);

    // create Run
    RedundancyRun run = new RedundancyRun();
    run.setRelationPublication(publication);
    run.setName("TestRedunRun");
    run.setProgram("BLASTN");
    run.setBlastDatabase("zfin_cdna_seq");
    Date date = new Date();
    logger.debug("date: "+date);
    run.setDate(date);
    run.setNomenclaturePublication(publication);
//    run.setType(Run.Type.REDUNDANCY);
    session.save(run);

    // create Candidate
    Candidate candidate = new Candidate();
    candidate.setRunCount(1);
    candidate.setLastFinishedDate(new Date());
    //candidate.setIdentifiedMarker(cDNA);
    candidate.setSuggestedName("novelGene");
    //should this be referencing an enum in markerType class or marker class?
    candidate.setMarkerType(Marker.Type.GENE.toString());

    //we should probably create a fake marker that can go away.
//    candidate.setIdentifiedMarker(markerRepository.getMarkerByID("ZDB-CDNA-040425-323"));
    session.save(candidate);

    // create RunCandidate
    RunCandidate runCandidate = new RunCandidate();
    runCandidate.setRun(run);
    runCandidate.setDone(false);
    runCandidate.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
    runCandidate.setCandidate(candidate);
    session.save(runCandidate);

    // create Query
    Query query = new Query();
    query.setRunCandidate(runCandidate);
    query.setAccession(accession1);
    runCandidate.getCandidateQueries().add(query);
    session.save(query);

    // create 5 Hits
    Hit hit1 = new Hit();
    hit1.setHitNumber(1);
    hit1.setQuery(query);
    hit1.setTargetAccession(accession1);
    query.getBlastHits().add(hit1);

    //when we have methods to create a gene, then create one instead of using exiting
    //that could be merged/deleted; or maybe we should modify the getMarkerByAbbreviation method
    //to check for aliases...

//    hit1.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
    hit1.setExpectValue(0.00);
    hit1.setScore(999);
    hit1.setPositivesNumerator(1);
    hit1.setPositivesDenominator(1);
    session.save(hit1);

    Hit hit2 = new Hit();
    hit2.setHitNumber(2);
    hit2.setQuery(query);
    hit2.setTargetAccession(accession2);
    query.getBlastHits().add(hit2);
//    hit2.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
    hit2.setExpectValue(1.3e-56);
    hit2.setScore(800);
    hit2.setPositivesNumerator(2);
    hit2.setPositivesDenominator(4);
    session.save(hit2);

    Hit hit3 = new Hit();
    hit3.setQuery(query);
    hit3.setHitNumber(3);
    hit3.setTargetAccession(accession3);
//    hit3.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
    hit3.setExpectValue(1.3e-26);
    hit3.setScore(500);
    query.getBlastHits().add(hit3);
    hit3.setPositivesNumerator(3);
    hit3.setPositivesDenominator(6);
    session.save(hit3);

    Hit hit4 = new Hit();
    hit4.setQuery(query);
    hit4.setHitNumber(4);
    hit4.setTargetAccession(accession4);
//    hit4.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
    hit4.setExpectValue(1.0e-16);
    hit4.setScore(300);
    query.getBlastHits().add(hit4);
    hit4.setPositivesNumerator(4);
    hit4.setPositivesDenominator(8);
    session.save(hit4);

    Hit hit5 = new Hit();
    hit5.setQuery(query);
    hit5.setHitNumber(5);
    hit5.setTargetAccession(accession5);
//    hit5.setZfinAccession(markerRepository.getMarkerByAbbreviation("reno"));
    hit5.setExpectValue(1.8e-6);
    hit5.setScore(100);
    hit5.setPositivesNumerator(5);
    hit5.setPositivesDenominator(10);
    query.getBlastHits().add(hit5);
    session.save(hit5);

     return runCandidate.getZdbID();
 }
    
}
