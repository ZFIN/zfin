package org.zfin.sequence.reno;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import static org.zfin.framework.HibernateUtil.getSessionFactory;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.TestConfiguration;
import org.zfin.orthology.Species;
import org.zfin.sequence.reno.presentation.CandidateBean;
import org.zfin.sequence.reno.presentation.CandidateController;
import org.zfin.sequence.Accession;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.Query;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.marker.Marker;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.apache.log4j.Logger;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;


public class MultiRunTest {

    private Logger logger = Logger.getLogger(MultiRunTest.class) ;
    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private ProfileRepository personRepository = RepositoryFactory.getProfileRepository();

    // testdata
    private NomenclatureRun nomenRun1 ;
    private NomenclatureRun nomenRun2 ;
    private Candidate candidateNomen ;
    private RunCandidate runCandidateNomen1;
    private RunCandidate runCandidateNomen2 ;
    private final String BASIC_NOTE = "basic note" ;
    private CandidateBean candidateBean ;
    private String cdnaName = "mgc:test";

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

    private void insert2NomenWithSharedCandidate(){
        Session session = HibernateUtil.currentSession();

        ReferenceDatabase refDb = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK.toString(),
                ReferenceDatabase.Type.CDNA,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);

        Marker gene = new Marker();
        gene.setAbbreviation("reno");
        gene.setName("Reno Test Name");
        //should this be an enum?
        gene.setMarkerType(RepositoryFactory.getMarkerRepository().getMarkerTypeByName(Marker.Type.GENE.toString()));
//        gene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);

//        Marker cDNA = new Marker();
//        cDNA.setAbbreviation(cdnaName);
//        cDNA.setName(cdnaName);
//        //should this be an enum?
//        cDNA.setMarkerType(RepositoryFactory.getMarkerRepository().getMarkerTypeByName(Marker.Type.CDNA.toString()));
//        cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
//        session.save(cDNA);


        Publication attributionPub = publicationRepository.getPublication("ZDB-PUB-070122-15");
        Publication orthologyPub = publicationRepository.getPublication("ZDB-PUB-030905-1");

        nomenRun1 = new NomenclatureRun();
        nomenRun1.setNomenclaturePublication(attributionPub);
        nomenRun1.setOrthologyPublication(orthologyPub);
        nomenRun1.setName("TestNomenRun1");
        nomenRun1.setProgram("BLASTP");
        nomenRun1.setBlastDatabase("sptr_hssptr_mssptr_zf");
//        nomenRun1.setType(Run.Type.NOMENCLATURE);
        nomenRun1.setDate(new Date());
        session.save(nomenRun1);

        nomenRun2 = new NomenclatureRun();
        nomenRun2.setNomenclaturePublication(attributionPub);
        nomenRun2.setOrthologyPublication(orthologyPub);
        nomenRun2.setName("TestNomenRun2");
        nomenRun2.setProgram("BLASTP");
        nomenRun2.setBlastDatabase("sptr_hssptr_mssptr_zf");
//        nomenRun2.setType(Run.Type.NOMENCLATURE);
        nomenRun2.setDate(new Date());
        session.save(nomenRun2);

        // create Candidate
        candidateNomen = new Candidate();
//        candidateNomen.setRunCount(1);
        candidateNomen.setLastFinishedDate(new Date());
        candidateNomen.setSuggestedName("renoRename");
        candidateNomen.setMarkerType(Marker.Type.GENE.toString());
        session.save(candidateNomen);

        // create RunCandidateNomen
        runCandidateNomen1 = new RunCandidate();
        runCandidateNomen1.setRun(nomenRun1);
        runCandidateNomen1.setDone(false);
        runCandidateNomen1.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
        runCandidateNomen1.setCandidate(candidateNomen);
        nomenRun1.getCandidates().add(runCandidateNomen1);
        session.save(runCandidateNomen1);

        Accession accession1 = new Accession();
        //accession1.setID((long) 1);
        accession1.setNumber("AC:TEST1");
        accession1.setDefline("defline jam");
        accession1.setLength(12);
        accession1.setReferenceDatabase(refDb);

        MarkerDBLink dblink1 = new MarkerDBLink() ;
        dblink1.setAccessionNumber("AC:TEST1");
        dblink1.setReferenceDatabase(refDb);
        dblink1.setMarker( gene) ;
        session.save(dblink1);
        session.save(accession1);

        // create Redundancy Query
        Query query1 = new Query();
        query1.setRunCandidate(runCandidateNomen1);
        query1.setAccession(accession1);
        runCandidateNomen1.getCandidateQueries().add(query1);
        session.save(query1);

        // create RunCandidateNomen
        runCandidateNomen2 = new RunCandidate();
        runCandidateNomen2.setRun(nomenRun2);
        runCandidateNomen2.setDone(false);
        runCandidateNomen2.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-2"));
        runCandidateNomen2.setCandidate(candidateNomen);
        nomenRun2.getCandidates().add(runCandidateNomen2);
        session.save(runCandidateNomen2);

        Accession accession2 = new Accession();
        //accession2.setID((long) 2);
        accession2.setNumber("AC:TEST2");
        accession2.setDefline("defline jam");
        accession2.setLength(12);
        accession2.setReferenceDatabase(refDb);

        MarkerDBLink dblink2 = new MarkerDBLink() ;
        dblink2.setAccessionNumber("AC:TEST2");
        dblink2.setReferenceDatabase(refDb);
        dblink2.setMarker( gene) ;
        session.save(dblink2);

        session.save(accession2);

        // create Redundancy Query
        Query query2 = new Query();
        query2.setRunCandidate(runCandidateNomen2);
        query2.setAccession(accession2);
        runCandidateNomen2.getCandidateQueries().add(query2);
        session.save(query2);


        candidateBean = new CandidateBean() ;
        candidateBean.setRunCandidate(runCandidateNomen1);
        candidateBean.setGeneZdbID("");
        //could associatedGeneField be refactored to: itemPickedFromSelectList
//        candidateBean.setAssociatedGeneField((RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation("reno")).getZdbID());
//        candidateBean.setAssociatedGeneField(CandidateBean.PROBLEM);
        candidateBean.setCandidateNote(BASIC_NOTE);
        //assume that they don't want to rename for Redun, then set explicitly to true in the
        //casses where they do want to rename.
        candidateBean.setRename(false);
        candidateBean.setAction(CandidateBean.DONE);
        candidateBean.setOrthologyPublicationZdbID(orthologyPub.getZdbID());
        candidateBean.setNomenclaturePublicationZdbID(candidateBean.getRunCandidate().getRun().getNomenclaturePublication().getZdbID());
        logger.info(candidateBean.getAction());

    }

    /** Verifies:
     *  - candidates can occur across multiple runs
     *  - that run_count is incremented via the trigger     
     *
     */
    @Test
    public void verifyCandidatesAccrossRuns(){
        Session session = HibernateUtil.currentSession();
        session.beginTransaction() ;
        try{
            insert2NomenWithSharedCandidate();
            RunCandidate runCandidate = nomenRun1.getCandidates().iterator().next() ;
            assertNotNull("RunCanidate failed to persist",runCandidate)  ;

            session.flush() ;
            String zdbId1 = nomenRun1.getCandidates().iterator().next().getCandidate().getZdbID() ;
            String zdbId2 = nomenRun2.getCandidates().iterator().next().getCandidate().getZdbID() ;
            assertEquals("candidate is shared across both runs",zdbId1,zdbId2) ;
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
     *  When associated runcandidate is marked "Done" the other
     *  the other runcandidates that share the same candidate
     *  should be left intact
     */
    @Test
    public void doneUpdatesPropagatedAccrossRuns(){
        Session session = HibernateUtil.currentSession();
        session.beginTransaction() ;
        try{
            insert2NomenWithSharedCandidate();
            session.flush() ;
            // this is the 'problem' code
//            candidateBean.setAssociatedGeneField();
            RunCandidate runCandidate1 =  nomenRun1.getCandidates().iterator().next() ;
            RunCandidate runCandidate2 =  nomenRun2.getCandidates().iterator().next() ;

            candidateBean.setGeneAbbreviation(cdnaName);
            candidateBean.setGeneName(cdnaName);
            assertFalse("runCandidate1 is NOT done",runCandidate1.isDone()) ;
            assertFalse("runCandidate2 is NOT done",runCandidate2.isDone()) ;

            // needs to refreshed 
            session.refresh(runCandidate1.getCandidate()) ;
            session.refresh(runCandidate1) ;
            session.refresh(runCandidate2) ;
            assertEquals("runCandidate1 occurrs 1st",1,runCandidate1.getOccurrenceOrder()) ;
            assertEquals("runCandidate2 occurrs 2nd",2,runCandidate2.getOccurrenceOrder()) ;
            assertEquals("run count should be 2",2,runCandidate1.getCandidate().getRunCount()) ;


            CandidateController candidateController = new CandidateController() ;
            candidateController.handleDone(candidateBean);
            session.flush() ;

            assertNotSame("run candidate is NOT shared across both runs",runCandidate1.getZdbID(),runCandidate2.getZdbID()); ;
            Candidate candidate1 = runCandidate1.getCandidate() ;
            Candidate candidate2 = runCandidate2.getCandidate() ;
            assertEquals("candidate is shared across both runs",candidate1.getZdbID(),candidate2.getZdbID()) ;
            assertTrue(!candidateBean.isRename());
            assertTrue("runCandidate1 IS FINISHED",runCandidate1.isDone()) ;
            assertFalse("runCandidate2 is NOT done",runCandidate2.isDone()) ;

            session.refresh(runCandidate1.getCandidate()) ;
            assertEquals("run count should be 2",2,runCandidate1.getCandidate().getRunCount()) ;
            assertEquals("run count should be 2",2,runCandidate1.getCandidate().getRunCount()) ;
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
     *  When a run is deleted, related run candidates, blast queries,
     *  and blast hits should all gone, while candidates should all remain, intact.
     *
     *  Note that deletions are handled via sql, and not hibernate, so this should
     *  occur via cascades (maybe activedata?).
     */
    @Test
    public void testDelete(){
        Session session = HibernateUtil.currentSession();
        session.beginTransaction() ;
        try{
            insert2NomenWithSharedCandidate();
            session.flush();

            RunCandidate runCandidate1 =  nomenRun1.getCandidates().iterator().next() ;
            session.refresh(runCandidate1) ;
            assertEquals("should be the first occurrence order",1,runCandidate1.getOccurrenceOrder()) ;
            String runCandidateZdbID1 = runCandidate1.getZdbID() ;
            String candidateZdbId1 = runCandidate1.getCandidate().getZdbID() ;

            Criteria r1 = session.createCriteria(Run.class) ;
            r1.add(Restrictions.eq("name","TestNomenRun1"));

            org.hibernate.Query rc1  = session.createQuery("from RunCandidate rc where rc.zdbID='"+runCandidateZdbID1+"'");
            org.hibernate.Query c1 = session.createQuery("from Candidate c where c.zdbID='"+candidateZdbId1+"'");


            List<Run> runs = r1.list() ;
            assertEquals("should be 1 run",runs.size(),1);
            List<RunCandidate> runCandidates = rc1.list() ;
            assertEquals("should be one run candidate",runCandidates.size(),1);
            List<Query> candidates = c1.list() ;
            assertEquals("should be one candidate",candidates.size(),1);


            session.delete(nomenRun1) ;
            session.flush() ;
            RunCandidate runCandidate2 = nomenRun2.getCandidates().iterator().next() ;
            assertNotNull("Run candidate 2 is not null",runCandidate2);
            Candidate candidate2 = runCandidate2.getCandidate() ;
            assertNotNull("candidate2 is not null",candidate2);

            runs = r1.list() ;
            assertEquals("should be no runs",runs.size(),0);

            runCandidates = rc1.list() ;
            assertEquals("should be no run candidate",runCandidates.size(),0);

            candidates = c1.list() ;
            assertEquals("should be one candidate still",candidates.size(),1);


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
    public void verifyRunCast(){
        Session session = HibernateUtil.currentSession();
        Publication nomenclaturePub = publicationRepository.getPublication("ZDB-PUB-070122-15");
        Publication relationPub = publicationRepository.getPublication("ZDB-PUB-070122-15");
        Publication orthologyPub = publicationRepository.getPublication("ZDB-PUB-030905-1");

        session.beginTransaction() ;
        try{
            NomenclatureRun nomenRun = new NomenclatureRun();
            nomenRun.setNomenclaturePublication(nomenclaturePub);
            nomenRun.setOrthologyPublication(orthologyPub);
            nomenRun.setName("NomenPub");
            nomenRun.setProgram("BLASTP");
            nomenRun.setBlastDatabase("sptr_hssptr_mssptr_zf");
//        nomenRun1.setType(Run.Type.NOMENCLATURE);
            nomenRun.setDate(new Date());
            session.save(nomenRun);

            RedundancyRun redunRun = new RedundancyRun();
            redunRun.setNomenclaturePublication(nomenclaturePub);
            redunRun.setRelationPublication(relationPub);
            redunRun.setName("RedunPub");
            redunRun.setProgram("BLASTP");
            redunRun.setBlastDatabase("sptr_hssptr_mssptr_zf");
//        nomenRun2.setType(Run.Type.NOMENCLATURE);
            redunRun.setDate(new Date());
            session.save(redunRun);

            session.flush();

            Run nomenRunCheck = (Run) session.createQuery("from Run r where r.name='NomenPub'").uniqueResult();
            assertTrue("nomen is of Run",nomenRunCheck instanceof Run) ;
            assertTrue("nomen is of NomenclatureRun",nomenRunCheck instanceof NomenclatureRun) ;
            assertTrue("nomen isNomen",nomenRunCheck.isNomenclature()) ;
            assertFalse("nomen !isRedun",nomenRunCheck.isRedundancy()) ;

            NomenclatureRun nomenRunCast = (NomenclatureRun) nomenRunCheck ;
            assertTrue("nomenRunCast is of NomenclatureRun",nomenRunCast instanceof NomenclatureRun) ;


            Run redunRunCheck = (Run) session.createQuery("from Run r where r.name='RedunPub'").uniqueResult();
            assertTrue("redun is of Run",redunRunCheck instanceof Run) ;
            assertTrue("redun is of RedundancyRun",redunRunCheck instanceof RedundancyRun) ;
            assertFalse("redun !isNomen",redunRunCheck.isNomenclature()) ;
            assertTrue("redun isRedun",redunRunCheck.isRedundancy()) ;

            RedundancyRun redunRunCast = (RedundancyRun) redunRunCheck;
            assertTrue("nomenRunCast is of NomenclatureRun",redunRunCast instanceof RedundancyRun) ;


        }catch(Exception e){
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally{
            session.getTransaction().rollback();
        }

    }
}
