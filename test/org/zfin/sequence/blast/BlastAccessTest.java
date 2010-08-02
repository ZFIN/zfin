package org.zfin.sequence.blast;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.Transcript;
import org.zfin.orthology.Species;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.framework.HibernateUtil.getSessionFactory;

/**
 * Tests the xdbget.c function in lib/DB_functions/C, and mapped in blast.hbm.xml
 * Calls the function with blastAbbrev, blastDBtype, and accession number as parameters.
 */
public class BlastAccessTest {

    private Logger logger = Logger.getLogger(BlastAccessTest.class);

    static {
        SessionFactory sessionFactory = getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.initApplicationProperties();

//        String file = "zfin-properties.xml";
//        String dirRel = System.getProperty("WEBINF") ;
//        ZfinProperties.init(dirRel, file);
        RepositoryFactory.getBlastRepository().setAllDatabaseLock(false);
    }

    @After
    public void closeSession() {
        RepositoryFactory.getBlastRepository().setAllDatabaseLock(false);
        HibernateUtil.closeSession();
    }

    @Test
    public void getBlastPath() {
        assertNotNull("blast path should not be null", ZfinProperties.getWebHostDatabasePath());
        assertFalse("blast path should not be whats in test", ZfinProperties.getWebHostDatabasePath().equals("/research/zunloads/test_blastfiles"));
    }

    /**
     * This test runs for all display groups
     */
    @Test
    public void getNucleotideSequenceThroughTranscriptForNucleotideShow() {
        // should always get a valid transcript this way
        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "where l.referenceDatabase.zdbID = :refDBZdbID " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);

        List<ReferenceDatabase> referenceDatabases =
                RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);

        boolean hasSequences = false;
        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            query.setParameter("refDBZdbID", referenceDatabase.getZdbID());
            List<Transcript> transcripts = query.list();

            for (Transcript transcript : transcripts) {
                List<Sequence> nucleotideSequences = null;
                try {
                    nucleotideSequences = MountedWublastBlastService.getInstance().getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
                } catch (BlastDatabaseException e) {
                    fail(e.toString());  //To change body of catch statement use File | Settings | File Templates.
                }
                logger.debug("nucleotideSequences.size(): " + nucleotideSequences.size());
                if (nucleotideSequences.size() > 0) {
                    hasSequences = true;
                    break;
                }
            }
        }
        assertTrue("Should have at least one sequence", hasSequences);

    }

    /**
     * This test runs for all display groups
     */
    @Test
    public void getProteinSequenceThroughTranscriptForProteinShow() {
        // should always get a valid transcript this way
        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "where l.referenceDatabase.zdbID = :refDBZdbID " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);

        List<ReferenceDatabase> referenceDatabases =
                RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE);

        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            query.setParameter("refDBZdbID", referenceDatabase.getZdbID());
            List<Transcript> transcripts = query.list();

            for (Transcript transcript : transcripts) {
                List<Sequence> proteinSequences = null;
                try {
                    proteinSequences = MountedWublastBlastService.getInstance().getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE);
                } catch (BlastDatabaseException e) {
                    fail(e.toString());  //To change body of catch statement use File | Settings | File Templates.
                }
                logger.debug("nucleotideSequences.size(): " + proteinSequences.size());
                if (proteinSequences.size() > 0) {
                    break;
                }
            }
        }


    }


    @Test
    public void addNucleotideSequenceThroughTranscript() {

        Session session = HibernateUtil.currentSession();

        final String sequenceData = "AAAAAAAATTTTTTTTTTCCCCCCCCGGGGGGGG";

        // should always get a valid transcript this way
        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);


        List<ReferenceDatabase> referenceDatabases =
                RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                        DisplayGroup.GroupName.ADDABLE_NUCLEOTIDE_SEQUENCE);


        Transaction transaction = session.beginTransaction();
        try {
            for (ReferenceDatabase referenceDatabase : referenceDatabases) {
                Transcript transcript = (Transcript) query.uniqueResult();

                // get first transcript DBLink
                logger.debug("addNucleotideSequenceThroughTranscript - referenceDB returned: " + referenceDatabase.toString());
                Sequence sequence = MountedWublastBlastService.getInstance().addSequenceToTranscript(transcript.getZdbID(), sequenceData, referenceDatabase.getZdbID());
                Defline defLine = sequence.getDefLine();

                List<Sequence> returnSequences = MountedWublastBlastService.getInstance().getSequencesForTranscript(transcript, DisplayGroup.GroupName.DISPLAYED_NUCLEOTIDE_SEQUENCE);
                logger.debug("addNucleotideSequenceThroughTranscript - returnSequences: " + returnSequences);

                assertTrue(returnSequences.size() > 0);

                boolean hasSequence = false;
                for (Sequence returnSequence : returnSequences) {
                    logger.debug("addNucleotideSequenceThroughTranscript - sample sequence: " + returnSequence.toString());
                    logger.debug("return defline: " + returnSequence.getDefLine() + " that defline: " + defLine);
                    logger.debug("return data: " + returnSequence.getData() + " that sequenceData: " + sequenceData);
                    if (
                            returnSequence.getDefLine().equals(defLine)
                                    &&
                                    returnSequence.getData().equalsIgnoreCase(sequenceData)
                            ) {
                        hasSequence = true;
                    }
                }
                assertTrue("Must have the sequence that it added", hasSequence);
            }
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            transaction.rollback();
        }
    }


    @Test
    public void addProteinSequenceThroughGene() {

        Session session = HibernateUtil.currentSession();

        final String sequenceData = "AYAYAYAYAYAYAYAYAYAYAYAYAY";

        // should always get a valid transcript this way
        String hql = "" +
                "select l.marker from MarkerDBLink  l where l.marker.zdbID like 'ZDB-GENE-%' " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);

        List<DisplayGroup.GroupName> displayGroups = new ArrayList<DisplayGroup.GroupName>();
        // this has nothing in it so we can't test it yet
        displayGroups.add(DisplayGroup.GroupName.GENE_EDIT_ADDABLE_PROTEIN_SEQUENCE);

        for (DisplayGroup.GroupName displayGroup : displayGroups) {
            List<ReferenceDatabase> referenceDatabases =
                    RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            displayGroup);

            Transaction transaction = session.beginTransaction();
            try {
                logger.debug("# of refDbs:" + referenceDatabases.size());
                for (ReferenceDatabase referenceDatabase : referenceDatabases) {
                    Marker marker = (Marker) query.uniqueResult();


                    // get first transcript DBLink
                    logger.debug("addProteinSequenceThroughTranscript - referenceDB returned: " + referenceDatabase.toString());
                    logger.debug("marker: " + marker);
                    logger.debug("sequenceData: " + sequenceData);
                    // insert without a pub id
                    Sequence sequence = MountedWublastBlastService.getInstance().addProteinToMarker(marker, sequenceData, "", referenceDatabase);
                    logger.debug("sequence: " + sequence);
                    Defline defLine = sequence.getDefLine();

                    List<Sequence> returnSequences = MountedWublastBlastService.getInstance().getSequencesForMarker(marker, DisplayGroup.GroupName.GENE_EDIT_ADDABLE_PROTEIN_SEQUENCE);
                    logger.debug("addProteinSequenceThroughTranscript - returnSequences: " + returnSequences.size());
//
                    assertTrue("Should have at least one sequence that was added", returnSequences.size() > 0);

                    boolean hasSequence = false;
                    for (Sequence returnSequence : returnSequences) {
                        logger.debug("addProteinSequenceThroughTranscript - sample sequence: " + returnSequence.toString());
                        logger.debug("return defline: " + returnSequence.getDefLine() + " that defline: " + defLine);
                        logger.debug("return data: " + returnSequence.getData() + " that sequenceData: " + sequenceData);
                        if (
                                returnSequence.getDefLine().equals(defLine)
                                        &&
                                        returnSequence.getData().equals(sequenceData)
                                ) {
                            hasSequence = true;
                        } else {
                            logger.debug("sequence doesn't match");
                        }
                    }
                    logger.debug("has sequence for refDB[" + referenceDatabase.getZdbID() + "]: " + hasSequence);
                    assertTrue("Must have the sequence that it added", hasSequence);
                }
            }
            catch (Exception e) {
                fail(e.toString());
            }
            finally {
                transaction.rollback();
            }
        }
    }

    @Test
    public void addProteinSequenceThroughTranscript() {

        Session session = HibernateUtil.currentSession();

        final String sequenceData = "AYAYAYAYAYAYAYAYAYAYAYAYAY";

        // should always get a valid transcript this way
        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);

        List<DisplayGroup.GroupName> displayGroups = new ArrayList<DisplayGroup.GroupName>();
        displayGroups.add(DisplayGroup.GroupName.TRANSCRIPT_EDIT_ADDABLE_PROTEIN_SEQUENCE);
        // this has nothing in it so we can't test it yet
//        displayGroups.add(DisplayGroup.GroupName.MARKER_EDIT_ADDABLE_PROTEIN_SEQUENCE) ;

        for (DisplayGroup.GroupName displayGroup : displayGroups) {
            List<ReferenceDatabase> referenceDatabases =
                    RepositoryFactory.getDisplayGroupRepository().getReferenceDatabasesForDisplayGroup(
                            displayGroup);

            Transaction transaction = session.beginTransaction();
            try {
                logger.debug("# of refDbs:" + referenceDatabases.size());
                for (ReferenceDatabase referenceDatabase : referenceDatabases) {
                    Transcript transcript = (Transcript) query.uniqueResult();

                    // get first transcript DBLink
                    logger.debug("addProteinSequenceThroughTranscript - referenceDB returned: " + referenceDatabase.toString());
                    logger.debug("transcript: " + transcript);
                    logger.debug("sequenceData: " + sequenceData);
                    Sequence sequence = MountedWublastBlastService.getInstance().addSequenceToTranscript(
                            transcript.getZdbID(), sequenceData, referenceDatabase.getZdbID());
                    logger.debug("sequence: " + sequence);
                    Defline defLine = sequence.getDefLine();

                    List<Sequence> returnSequences = MountedWublastBlastService.getInstance().getSequencesForTranscript(transcript, displayGroup);
                    logger.debug("addProteinSequenceThroughTranscript - returnSequences: " + returnSequences.size());

                    assertTrue("Should have at least one sequence that was added", returnSequences.size() > 0);

                    boolean hasSequence = false;
                    for (Sequence returnSequence : returnSequences) {
                        logger.debug("addProteinSequenceThroughTranscript - sample sequence: " + returnSequence.toString());
                        logger.debug("return defline: " + returnSequence.getDefLine() + " that defline: " + defLine);
                        logger.debug("return data: " + returnSequence.getData() + " that sequenceData: " + sequenceData);
                        if (
                                returnSequence.getDefLine().equals(defLine)
                                        &&
                                        returnSequence.getData().equals(sequenceData)
                                ) {
                            hasSequence = true;
                        } else {
                            logger.debug("sequence doesn't match");
                        }
                    }
                    logger.debug("has sequence for refDB[" + referenceDatabase.getZdbID() + "]: " + hasSequence);
                    assertTrue("Must have the sequence that it added", hasSequence);
                }
            }
            catch (Exception e) {
                fail(e.toString());
            }
            finally {
                transaction.rollback();
            }
        }
    }


    @Test
    public void getNucleotideAccessionNumber() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            String accession = NucleotideInternalAccessionGenerator.getInstance().generateAccession();
            logger.debug("nucleotide accession: " + accession);
            assertTrue("Bad nucleotide accession", accession.startsWith(NucleotideInternalAccessionGenerator.ZFIN_INTERNAL_ACCESSION_NUCLEOTIDE));
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            session.getTransaction().rollback();
        }
    }

    @Test
    public void getPolypeptideAccessionNumber() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            String accession = ProteinInternalAccessionGenerator.getInstance().generateAccession();
            logger.debug("protein accession: " + accession);
            assertTrue("Bad protein accession", accession.startsWith(ProteinInternalAccessionGenerator.ZFIN_INTERNAL_ACCESSION_PROTEIN));
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            session.getTransaction().rollback();
        }
    }


    //    @Test
    public void regenerateACuratedDatabases() {

        Database databaseToCurate = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE);

        try {
            MountedWublastBlastService.getInstance().regenerateDatabaseFromValidAccessions(databaseToCurate);
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    @Test
    public void validateCuratedDatabases() {

        try {
            if (false == MountedWublastBlastService.getInstance().validateCuratedDatabases()) {
                logger.warn("There was a problem validating the curated databases, please check the logs.");
            }
        } catch (BlastDatabaseException e) {
            logger.error("There was an error validating the curated databases.", e);
        }
    }

    @Test
    public void validateAllPhysicalDatabases() {
        try {
            List<String> strings = MountedWublastBlastService.getInstance().validateAllPhysicalDatabasesReadable();
            if (CollectionUtils.isNotEmpty(strings)) {
                logger.error("there was a problem validating all of the physical database");
                for (String string : strings) {
                    logger.warn(string);
                }
            }
        } catch (Exception e) {
            logger.error("there was a problem validating all of the physical database", e);
        }
    }

    //    @Test
    public void validateDatabase() {
        Database databaseA = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE);

        try {
            MountedWublastBlastService.getInstance().validateDatabase(databaseA);
        } catch (BlastDatabaseException e) {
            fail(e.fillInStackTrace().toString());
        }
    }


    // this takes WAY too long
    //    @Test
    public void regenerateCuratedDatabases() {
        try {
            MountedWublastBlastService.getInstance().regenerateCuratedDatabases();
        }
        catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Have to add a valid RNA accession in order for this to work.
     */
//    @Test
    public void curatedSequenceRegenerationForRNA() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.CURATED_MIRNA_MATURE,
                ForeignDBDataType.DataType.RNA,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH
        );

        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);
        assertEquals("Size is 1", 1, query.list().size());
        Transcript transcript = (Transcript) query.list().get(0);

        HibernateUtil.createTransaction();
        try {
            MountedWublastBlastService.getInstance().addSequenceToTranscript(transcript.getZdbID(), "ATATATAGA", referenceDatabase.getZdbID());
            MountedWublastBlastService.getInstance().regenerateDatabaseFromValidAccessions(referenceDatabase.getPrimaryBlastDatabase());
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            currentSession().getTransaction().rollback();
        }
    }


    /**
     * Have to add a valid RNA accession in order for this to work.
     */
//    @Test
    public void curatedSequenceRegenerationForProtein() {
        ReferenceDatabase referenceDatabase = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.PUBPROT,
                ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH
        );

        String hql = "" +
                "select l.transcript from TranscriptDBLink  l " +
                "";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setMaxResults(1);
        assertEquals("Size is 1", 1, query.list().size());
        Transcript transcript = (Transcript) query.list().get(0);

        HibernateUtil.createTransaction();
        try {
            MountedWublastBlastService.getInstance().addSequenceToTranscript(transcript.getZdbID(), "ATATATAGA", referenceDatabase.getZdbID());
            MountedWublastBlastService.getInstance().regenerateDatabaseFromValidAccessions(referenceDatabase.getPrimaryBlastDatabase());
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            currentSession().getTransaction().rollback();
        }
    }


    /**
     * We try and create a thread lock.
     * We execute the thread and keep checking to make sure that it gets locked at some point.
     * Since this process should take more than 10 ms, it should be pretty safe.
     * <p/>
     * First we do this for a single-thread and
     */
    @Test
    public void createSingleThreadLock() {
        Database databaseA = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.CURATEDMICRORNASTEMLOOP);
        assertFalse("database A is not locked", databaseA.isLocked());
        try {
            MountedWublastBlastService.getInstance().getLock(databaseA);
        } catch (BlastDatabaseException e) {
            fail(e.toString());
        } finally {
            assertTrue("database A is locked", databaseA.isLocked());
            MountedWublastBlastService.getInstance().unlockForce(databaseA);
            assertFalse("database A lock has been released", databaseA.isLocked());
        }
    }

    /**
     * In this test we explicitly lock the databse in the main thread and the
     * spawned thread has to try to grab the lock after a brief pause.
     */
    @Test
    public void createMultiThreadLock() {
        Database databaseA = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.CURATEDMICRORNASTEMLOOP);
        MountedWublastBlastService.getInstance().unlockForce(databaseA);

        assertFalse("database A lock has been released", databaseA.isLocked());

        HibernateUtil.createTransaction();
        try {

            Thread aThread = new Thread() {
                public void run() {
                    try {
                        logger.debug("entering thread");
                        Thread.sleep(200); // sleep long enough to let the other thread lock
                        logger.debug("awak thread");
                        Database databaseB = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.CURATEDMICRORNASTEMLOOP);
                        assertTrue("databaseB is locked", databaseB.isLocked());
                        logger.debug("databaseB getting lock");
                        MountedWublastBlastService.getInstance().getLock(databaseB);
                        logger.debug("databaseB locked");
                        assertTrue("databaseB is now locked by us", databaseB.isLocked());
                        logger.debug("databaseB releasing lock");
                        MountedWublastBlastService.getInstance().unlockForce(databaseB);
                        logger.debug("databaseB lock released");
                        assertFalse("databaseB is now unlocked", databaseB.isLocked());
                        logger.debug("databseB is now unlocked: " + databaseB.isLocked());
                    }
                    catch (Exception e) {
                        fail(e.toString());
                    }
                }
            };
            try {
                aThread.start();
            }
            catch (Exception e) {
                fail(e.toString());
            }

            logger.debug("POST-SPAWN: database is locked: " + databaseA.isLocked());

            assertFalse("database A is still unlocked: ", databaseA.isLocked());
            MountedWublastBlastService.getInstance().getLock(databaseA);
            assertTrue("database A is still locked: ", databaseA.isLocked());
            logger.debug("waiting for thread to finish: " + databaseA.isLocked());
            while (aThread.isAlive()) {
                Thread.sleep(50);
            }
            MountedWublastBlastService.getInstance().unlockForce(databaseA);
            logger.debug("databseA is should now be unlocked: " + databaseA.isLocked());
            assertFalse("database A is now unlocked again: ", databaseA.isLocked());
        }
        catch (Exception e) {
            fail(e.toString());
        }
        finally {
            currentSession().getTransaction().rollback();
        }
    }

//
//    // they aren't quite synced yet
//    //    @Test
//    public void validateCuratedDatabases(){
//        try {
//            MountedWublastBlastService.getInstance().validateCuratedDatabases();
//        } catch (BlastDatabaseException e) {
//            fail(e.fillInStackTrace().toString()) ;
//        }
//    }
//
//    // not quite ready yet
//    //    @Test
//    public void validateRemotePhysicalDatabaseReadability(){
//        List<String> failures = BlastServerSGEWublastService.getInstance().validateAllPhysicalDatabasesReadable() ;
//        if(CollectionUtils.isNotEmpty(failures)){
//            for(String failure : failures){
//                logger.error(failure);
//            }
//            fail("Number of failed databases: "+ failures.size());
//        }
//        else{
//            logger.info("No failed databases found.");
//        }
//    }

    @Test
    public void backupRestore() {
        try {
            Database database = RepositoryFactory.getBlastRepository().getDatabase(Database.AvailableAbbrev.CURATEDMICRORNASTEMLOOP);
            DatabaseStatistics databaseStatistics = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database);
            int numAccessions = databaseStatistics.getNumAccessions();
            int numSequences = databaseStatistics.getNumSequences();
            MountedWublastBlastService.getInstance().backupDatabase(database);
            MountedWublastBlastService.getInstance().restoreDatabase(database);
            WebHostDatabaseStatisticsCache.getInstance().clearCache();
            databaseStatistics = WebHostDatabaseStatisticsCache.getInstance().getDatabaseStatistics(database);
            assertEquals(numAccessions, databaseStatistics.getNumAccessions());
            assertEquals(numSequences, databaseStatistics.getNumSequences());
        }
        catch (Exception ioe) {
            fail(ioe.fillInStackTrace().toString());
        }
    }

    @Test
    public void getProperDefline() {
        Marker marker = new Marker();
        marker.setZdbID("ZDB-GENE-990415-200");
        MarkerDBLink markerDBLink = new MarkerDBLink();
        markerDBLink.setMarker(marker);
        String accession = "abc123";
        markerDBLink.setAccessionNumber("abc123");
        Defline defline = new MarkerDefline(markerDBLink);
        String returnedAccession = defline.toString().split("\\|")[1];
        assertEquals("Accessions should be the same", accession, returnedAccession);
    }
}
