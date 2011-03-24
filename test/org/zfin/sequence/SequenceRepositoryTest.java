package org.zfin.sequence;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Class SequenceRepositoryTest.
 */

public class SequenceRepositoryTest extends AbstractDatabaseTest {

    private final static Logger logger = Logger.getLogger(SequenceRepositoryTest.class);
    private SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();


    @Test
    public void testAccessionEntity() {
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            Accession accession1 = new Accession();
            String number = "AC:TEST";
            String abbrev = "AC:TEST_ABBREV";
            accession1.setNumber(number);
            accession1.setAbbreviation(abbrev);
            ReferenceDatabase genBankRefDB = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
            accession1.setReferenceDatabase(genBankRefDB);
            session.save(accession1);
            String hsqlString = "from Accession acc where acc.number = :number";
            Query query = session.createQuery(hsqlString);
            query.setString("number", number);
//            query.setMaxResults(1) ;
            Accession accession = (Accession) query.uniqueResult();
            assertNotNull("database contains at least one accession", accession);
            assertEquals("abbrevs are equal", abbrev, accession.getAbbreviation());
        } finally {
            session.getTransaction().rollback();
        }
    }

    @Test
    public void testReferenceDatabaseEntity() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.setMaxResults(1);
        ReferenceDatabase referenceDatabase = (ReferenceDatabase) criteria.uniqueResult();
        assertNotNull("database contains at least one reference database", referenceDatabase);
    }

    @Test
    public void testForeignDBEntity() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ForeignDB.class);
        criteria.setMaxResults(1);
        ForeignDB foreignDB = (ForeignDB) criteria.uniqueResult();
        assertNotNull("database contains at least one foreignDB ", foreignDB);
    }

    @Test
    public void testGetReferenceDatabaseByAlternateKey() {
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36", refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetReferenceDatabase() {
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36", refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetForeignDBByName() {
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ForeignDB foreignDB = sr.getForeignDBByName(ForeignDB.AvailableName.GENBANK);
        assertTrue("ForeignDB name is:Genbank", foreignDB.getDbName() == ForeignDB.AvailableName.GENBANK);
    }

    //this test will only work when the data is not reloaded;
    @Test
    public void testGetAccessionByAlternateKey() {
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36", refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void getReferenceDatabasesWithInternalBlast() {
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getSequenceRepository().getReferenceDatabasesWithInternalBlast();
        assertTrue("should be at least 10 of these things", referenceDatabases.size() > 10);
        for (ReferenceDatabase referenceDatabase : referenceDatabases) {
            assertNotNull("must have blast databases", referenceDatabase.getPrimaryBlastDatabase());
        }
    }

    @Test
    public void getMarkerSequenceForMorpholino() {
        MarkerSequence markerSequence = (MarkerSequence) HibernateUtil.currentSession()
                .createCriteria(MarkerSequence.class)
                .setMaxResults(1)
                .uniqueResult();
        assertNotNull(markerSequence);
        assertNotNull(markerSequence.getSequence());
        assertNotNull(markerSequence.getZdbID());
        assertNotNull(markerSequence.getMarker());
        logger.debug(markerSequence.getSequence());
        logger.debug(markerSequence.getZdbID());
        logger.debug(markerSequence.getMarker());
    }

    @Test
    public void testGenbankAllDownload() {
        List<String> dblinks = sequenceRepository.getGenbankSequenceDBLinks();
        assertNotNull(dblinks);
        assertTrue(dblinks.size() > 1000);
        assertTrue(dblinks.contains("AY627769"));
    }


    @Test
    public void getGenbankCdnaDBLinks() {
        List<String> dblinks = sequenceRepository.getGenbankCdnaDBLinks();
        assertNotNull(dblinks);
        assertTrue(dblinks.size() > 1000);
        assertTrue(dblinks.contains("NM_131644"));
    }

    //    @Test
    public void getGenbankXpatCdnaDBLinks() {
        Set<String> dblinks = sequenceRepository.getGenbankXpatCdnaDBLinks();
        assertNotNull(dblinks);
        logger.info("size: " + dblinks.size());
        assertTrue(dblinks.size() > 40000);
        assertTrue(dblinks.size() < 50000);
        assertTrue(dblinks.contains("AB000218"));
    }

    @Test
    public void getFirst10Sequences() {
        List<String> markerTypes = sequenceRepository.getAllNSequences(10);
        assertNotNull(markerTypes);
        assertTrue(markerTypes.size() == 10);
    }

}


