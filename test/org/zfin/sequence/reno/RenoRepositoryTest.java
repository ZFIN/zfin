package org.zfin.sequence.reno;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Accession;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.reno.repository.RenoRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Class RenoRepositoryTest.
 */

public class RenoRepositoryTest extends AbstractDatabaseTest {

    private static RenoRepository repository= RepositoryFactory.getRenoRepository();;


    @Test
    // Test that there are Redundancy runs in the database
    // Test the test redundancy run
    public void getRedundancyRuns() {
        HibernateUtil.createTransaction();
        try {
            List<RedundancyRun> redundancyRuns = repository.getRedundancyRuns();
            int oldSize = redundancyRuns.size();
            insertTestData();
            redundancyRuns = repository.getRedundancyRuns();
            // Check that there is at least one Redundancy Run (the test run)
            assertNotNull("Redundancy Runs not Null", redundancyRuns);
            assertNotSame("Redundancy runs returned 0", 0, repository.getRedundancyRuns().size());
            boolean found = false;
            for (Run run : redundancyRuns) {
                if (run.getName().equals("TestRedundandcy")) {
                    found = true;
                }
            }
            // Test Run found
            assertTrue("Added Redundancy Run is found", found);
            assertEquals("Added One Redundancy Run", oldSize + 1, redundancyRuns.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    // Test that there are nomenclature runs in the database
    public void getNomenclatureRuns() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            List<NomenclatureRun> nomenclatureRuns = repository.getNomenclatureRuns();
            int oldSize = nomenclatureRuns.size();
            insertTestData();
            nomenclatureRuns = repository.getNomenclatureRuns();
            assertNotNull("Nomenclature Runs not Null", nomenclatureRuns);
            assertNotSame("Nomenclature runs returned 0", 0, repository.getNomenclatureRuns().size());
            boolean found = false;
            for (Run run : nomenclatureRuns) {
                if (run.getName().equals("TestNomenclature")) {
                    found = true;
                }
            }
            assertTrue("Added Nomenclature Run is found", found);
            assertEquals("Added One Nomenclature Run", oldSize + 1, nomenclatureRuns.size());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }

    }


    @Test
    // Check that a query for a non-existent Run returns a null.
    public void didNotGetRunByID() {
        assertNull("No zdbid to return", repository.getRunCandidateByID("notazdbid"));
    }

    /**
     * Test querying queryCandidates.
     * insertTestData
     * Tests that QueueCandidates are not found for a null-Run.
     * Tests that QueueCandidates are only found if a candidate is
     * not Done and is not locked.
     */
    @Test
    public void queryCandidates() {
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            assertEquals("No query candidates", 0, repository.getQueueCandidateCount(null));
            Map<String, Object> returnMap = insertTestData();
            Run run = (Run) returnMap.get("run1");
            RunCandidate runCandidate = (RunCandidate) returnMap.get("runCandidate1");
            assertEquals("One query candidate", 1, repository.getQueueCandidateCount(run));
            runCandidate.setDone(true);
            assertEquals("No query candidate because done", 0, repository.getQueueCandidateCount(run));
            runCandidate.setDone(false);
            Person person1 = (Person) returnMap.get("person1");
            repository.lock(person1, runCandidate);
            assertEquals("No query candidate because locked", 0, repository.getQueueCandidateCount(run));
            repository.unlock(person1, runCandidate);
            assertEquals("One query candidate again", 1, repository.getQueueCandidateCount(run));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }


    @Test
    /**
     * Test pending candidates, i.e. candidates that are not done
     * put have a lock set. 
     */
    public void pendingCandidates() {
        assertEquals("No Pending candidates", 0, repository.getPendingCandidates(null).size());
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            assertEquals("No pending candidates", 0, repository.getPendingCandidateCount(null));
            Map<String, Object> returnMap = insertTestData();
            Run run1 = (Run) returnMap.get("run1");
            RunCandidate runCandidate = (RunCandidate) returnMap.get("runCandidate1");
            assertEquals("No pending candidate because not locked", 0, repository.getPendingCandidateCount(run1));
            runCandidate.setDone(true);
            session.saveOrUpdate(runCandidate);
            assertEquals("No pending candidate because finished", 0, repository.getPendingCandidateCount(run1));
            runCandidate.setDone(false);
            session.saveOrUpdate(runCandidate);
            Person person1 = (Person) returnMap.get("person1");
            repository.lock(person1, runCandidate);
            assertEquals("One pending candidate because locked and not finished", 1, repository.getPendingCandidateCount(run1));
            repository.unlock(person1, runCandidate);
            assertEquals("No pending candidate because unlocked and not finished", 0, repository.getPendingCandidateCount(run1));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

    @Test
    /**
     * Test finished Candidates, i.e. candidates that are marked as Done.
     */
    public void finishedCandidates() {
        assertEquals("No Finished candidates", 0, repository.getFinishedCandidateCount(null));
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            assertEquals("No finished candidates", 0, repository.getFinishedCandidateCount(null));
            Map<String, Object> returnMap = insertTestData();
            Run run1 = (Run) returnMap.get("run1");
            RunCandidate runCandidate = (RunCandidate) returnMap.get("runCandidate1");
            assertEquals("No finished candidate because not finished", 0, repository.getFinishedCandidateCount(run1));
            runCandidate.setDone(true);
            session.saveOrUpdate(runCandidate);
            assertEquals("One finished candidate because finished and not locked", 1,
                    repository.getFinishedCandidateCount(run1));
            runCandidate.setDone(false);
            session.saveOrUpdate(runCandidate);
            Person person1 = (Person) returnMap.get("person1");
            repository.lock(person1, runCandidate);
            assertEquals("No finished candidate because locked and not finished", 0,
                    repository.getFinishedCandidateCount(run1));
            repository.unlock(person1, runCandidate);
            assertEquals("No finished candidate because unlocked and not finished", 0,
                    repository.getFinishedCandidateCount(run1));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

    /**
     * Tests runCandidate locking.
     * insertTestData
     * Test that runCandidate is not locked.
     * Lock file with person 1.(success)
     * Lock file with person 1.(success)
     * Lock file with person 2. (fail)
     * Test that file IS locked. (success)
     * Unlock file with person 1. (success)
     * Test that file is NOT locked. (success)
     * Unlock file with person 1. (fail)
     * Lock file with person 2. (success)
     * Unlock file with person 1. (fail)
     * Test that file is NOT locked. (success)
     */
    @Test
    public void lockRunCandidate() {
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            Map<String, Object> returnMap = insertTestData();
            RunCandidate runCandidate = (RunCandidate) returnMap.get("runCandidate1");
            assertFalse("RunCandidate is not locked", runCandidate.isLocked());
            Person person1 = (Person) returnMap.get("person1");
            assertTrue("RunCandidate is locked by person 1", repository.lock(person1, runCandidate));
            assertTrue("RunCandidate is locked by same person 1", repository.lock(person1, runCandidate));
            Person person2 = (Person) returnMap.get("person2");
            assertFalse("RunCandidate is locked by person 2 and fails", repository.lock(person2, runCandidate));
            assertTrue("RunCandidate is file is locked", runCandidate.isLocked());
            assertTrue("RunCandidate is unlocked by person 1", repository.unlock(person1, runCandidate));
            assertFalse("RunCandidate is file not locked", runCandidate.isLocked());
            assertFalse("RunCandidate can not be unlocked if not locked", repository.unlock(person1, runCandidate));
            assertFalse("RunCandidate can not be unlocked if not locked", repository.unlock(person2, runCandidate));
            assertTrue("RunCandidate locked by person 2", repository.lock(person2, runCandidate));
            assertFalse("RunCandidate can not be unlocked by person 1 if locked by person 2", repository.unlock(person1, runCandidate));
            assertTrue("RunCandidate is locked", runCandidate.isLocked());
            assertTrue("RunCandidate unlocked by person 2", repository.unlock(person2, runCandidate));
            assertFalse("RunCandidate is unlocked", runCandidate.isLocked());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }

    }

    @Test
    public void testRunSetAttribution() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            Map<String, Object> returnMap = insertTestData();
            Run run1 = (Run) returnMap.get("run1");
            assertTrue("Is redundancy run", run1.isRedundancy());
            assertFalse("Not nomenclature run", run1.isNomenclature());
            RedundancyRun redunRun = (RedundancyRun) run1;
            Publication publication2 = (Publication) returnMap.get("publication2");
            assertNotSame("Run attribution has an expected initial value", publication2, redunRun.getNomenclaturePublication());

            redunRun.setRelationPublication(publication2);
            assertSame("Run attribution update is successful", publication2, redunRun.getRelationPublication());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

    @Test
    public void testRunSetOrthologyAttribution() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            Map<String, Object> returnMap = insertTestData();
            Run run2 = (Run) returnMap.get("run2");
            assertTrue("Is nomenclature run", run2.isNomenclature());
            assertFalse("Not redundancy run", run2.isRedundancy());
            NomenclatureRun nomenRun = (NomenclatureRun) run2;
            Publication publication1 = (Publication) returnMap.get("publication1");
            assertNotSame("Run orthology attribution has an expected initial value", publication1, nomenRun.getNomenclaturePublication());

            nomenRun.setOrthologyPublication(publication1);
            assertSame("Run orthology attribution update is successful", publication1, nomenRun.getOrthologyPublication());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }


    @Test
    public void testSortedRunCandidates() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            Map<String, Object> returnMap = insertTestData();
            Run run1 = (Run) returnMap.get("run1");
            List runCandidates;
            Hit bestHit;
            RunCandidate runCandidate;
            runCandidates = repository.getSortedRunCandidates(run1, "other", 3);
            runCandidate = (RunCandidate) runCandidates.get(0);
            bestHit = runCandidate.getBestHit();
            Hit hit1 = (Hit) returnMap.get("hit1");
            assertEquals(bestHit.getExpectValue(), hit1.getExpectValue(),0.001f);
            assertEquals(bestHit.getScore(), hit1.getScore());

            // should choose the same score, because still has the best expect value
            hit1.setScore(600);
            session.update(hit1);
            runCandidates = repository.getSortedRunCandidates(run1, "other", 3);
            runCandidate = (RunCandidate) runCandidates.get(0);
            bestHit = runCandidate.getBestHit();
            assertEquals(bestHit.getExpectValue(), hit1.getExpectValue(),0.001f);
            assertEquals(bestHit.getScore(), hit1.getScore());

            // make hit1 and hit2 the same expect value, so now should take hit2 value as 800 > 600
            Hit hit2 = (Hit) returnMap.get("hit2");
            hit2.setExpectValue(0);
            session.update(hit2);
            runCandidates = repository.getSortedRunCandidates(run1, "other", 3);
            runCandidate = (RunCandidate) runCandidates.get(0);
            bestHit = runCandidate.getBestHit();
            assertEquals(bestHit.getExpectValue(), hit2.getExpectValue(),0.001f);// would work for either hit1 or hit2
            assertEquals(bestHit.getScore(), hit2.getScore());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

//    @Test
//    public void testSortedRunNoZfinCandidates() {
//        Session session = HibernateUtil.currentSession();
//        session.beginTransaction();
//        try {
//            Map<String, Object> returnMap = insertTestData();
//
//
//
//            // assume this isn't going away for awhile
//            Entrez entrez2 = (Entrez) session.get("org.zfin.sequence.Entrez","9") ;
//            assertNotNull(entrez2);
//            Entrez entrez3 = (Entrez) session.get("org.zfin.sequence.Entrez","13") ;
//            assertNotNull(entrez3);
//
//            EntrezProtRelation entrezProt2Relation = new EntrezProtRelation() ;
//            entrezProt2Relation.setOrganism(Species.HUMAN);
//            Hit hit2 = (Hit) returnMap.get("hit2");
//            Accession hit2Accession = hit2.getTargetAccession() ;
//            entrezProt2Relation.setProteinAccNum(hit2Accession.getNumber());
//            entrezProt2Relation.setEntrezAccession(entrez2);
//            session.save(entrezProt2Relation);
//
//            EntrezProtRelation entrezProt3Relation = new EntrezProtRelation() ;
//            entrezProt3Relation.setOrganism(Species.HUMAN);
//            Hit hit3 = (Hit) returnMap.get("hit3");
//            Accession hit3Accession = hit3.getTargetAccession() ;
//            entrezProt3Relation.setProteinAccNum(hit3Accession.getNumber());
//            entrezProt3Relation.setEntrezAccession(entrez3);
//            session.save(entrezProt3Relation);
//            session.flush();
//
//
//
//            List runCandidates ;
//            Hit bestHit ;
//            RunCandidate runCandidate ;
//            Run run1 = (Run) returnMap.get("run1");
//
//
//            runCandidates =  repository.getSortedNonZFRunCandidates( run1.getZdbID(),"other", 3  ) ;
//            runCandidate = (RunCandidate) runCandidates.get(0) ;
//            bestHit = runCandidate.getBestHit() ;
//            assertEquals( bestHit.getExpectValue() , hit2.getExpectValue() );
//            assertEquals( bestHit.getScore() , hit2.getScore() );
//
//            // should choose the same score, because still has the best expect value
//            hit2.setScore(200);
//            session.update(hit2);
//            runCandidates =  repository.getSortedNonZFRunCandidates( run1.getZdbID(),"other", 3  ) ;
//            runCandidate = (RunCandidate) runCandidates.get(0) ;
//            bestHit = runCandidate.getBestHit() ;
//            assertEquals( bestHit.getExpectValue() , hit2.getExpectValue() );
//            assertEquals( bestHit.getScore() , hit2.getScore() );
//
//            // make hit1 and hit2 the same expect value, so now should take hit2 value as 800 > 600
//            hit2.setExpectValue(0);
//            hit3.setExpectValue(0);
//            session.update(hit2);
//            session.update(hit3);
//            runCandidates =  repository.getSortedNonZFRunCandidates( run1.getZdbID(),"other", 3  ) ;
//            runCandidate = (RunCandidate) runCandidates.get(0) ;
//            bestHit = runCandidate.getBestHit() ;
//            assertEquals( bestHit.getExpectValue() , hit2.getExpectValue() );// would work for either hit1 or hit2
//            assertEquals( bestHit.getScore() , hit3.getScore() );  // should be this one
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            fail(e.getMessage());
//        }
//        finally {
//            // rollback on success or exception to leave no new records in the database
//            session.getTransaction().rollback();
//        }
//    }


    /**
     * Returns ZdbID
     *
     * @return Map of Runs, candidates, Persons
     */
    private Map<String, Object> insertTestData() {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        Session session = HibernateUtil.currentSession();

        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Publication publication1 = publicationRepository.getPublication("ZDB-PUB-070122-15");
        Publication publication2 = publicationRepository.getPublication("ZDB-PUB-070210-20");

        Person person1 = RepositoryFactory.getProfileRepository().getPerson("ZDB-PERS-991202-1");
        returnMap.put("person1", person1);
        Person person2 = RepositoryFactory.getProfileRepository().getPerson("ZDB-PERS-960805-676");
        returnMap.put("person2", person2);


        Accession accession1 = new Accession();
        accession1.setID((long) 1);
        accession1.setNumber("test accession name");
        accession1.setDefline("defline jam");
        accession1.setLength(12);

        Accession accession2 = new Accession();
        accession2.setID((long) 2);
        accession2.setNumber("test accession name");
        accession2.setDefline("defline jam");
        accession2.setLength(12);

        Accession accession3 = new Accession();
        accession3.setID((long) 3);
        accession3.setNumber("test accession name");
        accession3.setDefline("defline jam");
        accession3.setLength(12);

        Accession accession4 = new Accession();
        accession4.setID((long) 4);
        accession4.setNumber("test accession name");
        accession4.setDefline("defline jam");
        accession4.setLength(12);

        Accession accession5 = new Accession();
        accession5.setID((long) 5);
        accession5.setNumber("test accession name");
        accession5.setDefline("defline jam");
        accession5.setLength(12);

        // create Run
        RedundancyRun run1 = new RedundancyRun();
        run1.setRelationPublication(publication1);
        run1.setNomenclaturePublication(publication2);
        run1.setName("TestRedundandcy");
//        run1.setType(Run.Type.REDUNDANCY);
        run1.setProgram("BLASTN");
        Date date = new Date();
        run1.setDate(date);
        run1.setBlastDatabase("zfin_cdna_seq");
        session.save(run1);
        returnMap.put("run1", run1);


        NomenclatureRun run2 = new NomenclatureRun();
        run2.setNomenclaturePublication(publication1);
        run2.setOrthologyPublication(publication2);
        run2.setName("TestNomenclature");
//        run2.setType(Run.Type.NOMENCLATURE);
        run2.setProgram("BLASTP");
        run2.setBlastDatabase("sptr_hssptr_mssptr_zf");
        Date date2 = new Date();
        run2.setDate(date2);
        session.save(run2);
        returnMap.put("run2", run2);

        // create Candidate
        Candidate candidate = new Candidate();
        candidate.setRunCount(1);
        candidate.setLastFinishedDate(new Date());
        candidate.setProblem(false);
        candidate.setMarkerType(Marker.Type.GENE.toString());
        candidate.setSuggestedName("Terry");
        session.save(candidate);

        // create RunCandidate
        RunCandidate runCandidate1 = new RunCandidate();
        runCandidate1.setRun(run1);
        runCandidate1.setDone(false);
        runCandidate1.setLockPerson(null);
        runCandidate1.setCandidate(candidate);
        session.save(runCandidate1);
        returnMap.put("runCandidate1", runCandidate1);

        // create RunCandidate
        RunCandidate runCandidate2 = new RunCandidate();
        runCandidate2.setRun(run2);
        runCandidate2.setDone(false);
        runCandidate2.setLockPerson(null);
        runCandidate2.setCandidate(candidate);
        session.save(runCandidate2);
        returnMap.put("runCandidate2", runCandidate2);

        // create Query
        org.zfin.sequence.blast.Query query = new org.zfin.sequence.blast.Query();
        query.setRunCandidate(runCandidate1);
        query.setAccession(accession1);
        runCandidate1.getCandidateQueries().add(query);
        session.save(query);

        // create 5 Hits
        Hit hit1 = new Hit();
        hit1.setQuery(query);
        hit1.setHitNumber(1);
        hit1.setTargetAccession(accession1);
        query.getBlastHits().add(hit1);

        //when we have methods to create a gene, then create one instead of using exiting
        //that could be merged/deleted; or maybe we should modify the getMarkerByAbbreviation method
        //to check for aliases...

        hit1.setExpectValue(0.00);
        hit1.setScore(999);
        hit1.setPositivesNumerator(1);
        hit1.setPositivesDenominator(1);
        session.save(hit1);
        returnMap.put("hit1", hit1);

        Hit hit2 = new Hit();
        hit2.setQuery(query);
        hit2.setHitNumber(2);
        hit2.setTargetAccession(accession2);
        query.getBlastHits().add(hit2);
        hit2.setExpectValue(1.3e-56);
        hit2.setScore(800);
        hit2.setPositivesNumerator(2);
        hit2.setPositivesDenominator(4);
        session.save(hit2);
        returnMap.put("hit2", hit2);

        Hit hit3 = new Hit();
        hit3.setQuery(query);
        hit3.setHitNumber(3);
        hit3.setTargetAccession(accession3);
        hit3.setExpectValue(1.3e-26);
        hit3.setScore(500);
        hit3.setPositivesNumerator(3);
        hit3.setPositivesDenominator(6);
        query.getBlastHits().add(hit3);
        session.save(hit3);
        returnMap.put("hit3", hit3);

        Hit hit4 = new Hit();
        hit4.setQuery(query);
        hit4.setHitNumber(4);
        hit4.setTargetAccession(accession4);
        hit4.setExpectValue(1.0e-16);
        hit4.setScore(300);
        hit4.setPositivesNumerator(4);
        hit4.setPositivesDenominator(8);
        query.getBlastHits().add(hit4);
        session.save(hit4);
        returnMap.put("hit4", hit4);

        Hit hit5 = new Hit();
        hit5.setQuery(query);
        hit5.setHitNumber(5);
        hit5.setTargetAccession(accession5);
        hit5.setExpectValue(1.8e-6);
        hit5.setScore(100);
        hit5.setPositivesNumerator(5);
        hit5.setPositivesDenominator(10);
        query.getBlastHits().add(hit5);
        session.save(hit5);
        returnMap.put("hit5", hit5);

        return returnMap;
    }


    @Test
    public void zdbGeneratorRollBack() {
        Session session = null;
        try {
            session = HibernateUtil.currentSession();
            session.beginTransaction();
            Map<String, Object> returnMap1 = insertTestData();
            session.getTransaction().rollback();
            HibernateUtil.closeSession();  // use the HibernateUtil since it knows about closing and opening sessions

            session = HibernateUtil.currentSession(); // get a new open session from the sssion factory
            session.beginTransaction();
            Map<String, Object> returnMap2 = insertTestData();

            assertEquals(
                    "person1 should have same zdbID for both maps",
                    ((Person) returnMap1.get("person1")).getZdbID(),
                    ((Person) returnMap2.get("person1")).getZdbID()
            );

            assertFalse(
                    "person1 and person2 should not have the same zdbID in the first map",
                    ((Person) returnMap1.get("person1")).getZdbID().equals(
                            ((Person) returnMap1.get("person2")).getZdbID()
                    )
            );

            assertFalse(
                    "person1 and person2 should not have the same zdbID in the second map",
                    ((Person) returnMap2.get("person1")).getZdbID().equals(
                            ((Person) returnMap2.get("person2")).getZdbID()
                    )
            );

            assertEquals(
                    "person2 should have same zdbID for both maps",
                    ((Person) returnMap1.get("person2")).getZdbID(),
                    ((Person) returnMap2.get("person2")).getZdbID()
            );

            assertEquals(
                    "run1 should have same zdbID for both maps",
                    ((Run) returnMap1.get("run1")).getZdbID(),
                    ((Run) returnMap2.get("run1")).getZdbID()
            );

            assertEquals(
                    "run2 should have same zdbID for both maps",
                    ((Run) returnMap1.get("run2")).getZdbID(),
                    ((Run) returnMap2.get("run2")).getZdbID()
            );

            assertEquals(
                    "runCandidate1 should have same zdbID for both maps",
                    ((RunCandidate) returnMap1.get("runCandidate1")).getZdbID(),
                    ((RunCandidate) returnMap2.get("runCandidate1")).getZdbID()
            );

            assertEquals(
                    "runCandidate2 should have same zdbID for both maps",
                    ((RunCandidate) returnMap1.get("runCandidate2")).getZdbID(),
                    ((RunCandidate) returnMap2.get("runCandidate2")).getZdbID()
            );


        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            session.getTransaction().rollback();
        }

    }


    /**
     * mostly just want to test failure
     */
    @Test
    public void populateLinkageGroups() {
        // in general, can just grab the first one
        Criteria criteria = HibernateUtil.currentSession().createCriteria(RunCandidate.class);
//        criteria.add(Restrictions.eq("zdbID", "ZDB-RUNCAN-080514-255"));
        criteria.setMaxResults(1);

        RunCandidate rc = (RunCandidate) criteria.list().get(0);
        assertNotNull(rc);
        RenoService.populateLinkageGroups(rc);
    }

} 


