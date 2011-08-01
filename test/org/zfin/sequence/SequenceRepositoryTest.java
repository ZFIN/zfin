package org.zfin.sequence;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

import static org.hamcrest.Matchers.*;
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
        assertFalse(dblinks.contains("EE708906"));
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

    @Test
    public void getDBLinksForMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List<DBLink> dblinks = sequenceRepository.getDBLinksForMarker(m.getZdbID(),ForeignDBDataType.SuperType.PROTEIN);
        assertTrue(dblinks.size()>8);
        assertTrue(dblinks.size()<20);
    }

    @Test
    public void getNumberDBLinksForMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = sequenceRepository.getNumberDBLinks(m);
        assertTrue(count > 7);
        assertTrue(count < 20);

    }

    @Test
    public void getSummaryMarkerDBLinksForMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List<DBLink> dbLinkList = sequenceRepository.getSummaryMarkerDBLinksForMarker(m);
        assertThat(dbLinkList.size(),greaterThan(1));
        assertThat(dbLinkList.size(),lessThan(5));

    }

    @Test
    public void getDBLinksForMarkerAndDisplayGroup(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        List<DBLink> dbLinkList = sequenceRepository.getDBLinksForMarkerAndDisplayGroup(m
                ,DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE);
        assertThat(dbLinkList.size(), greaterThan(5));
        assertThat(dbLinkList.size(), lessThan(15));
    }


    @Test
    public void getDBLinksForFirstRelatedMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        Collection<MarkerDBLink> dbLinkList = sequenceRepository.getDBLinksForFirstRelatedMarker(m
                ,DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE
                ,MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT
                ,MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT
                ,MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
        );
//        assertEquals(1, dbLinkList.size());
//        assertEquals("ZDB-DBLINK-060130-74944",dbLinkList.iterator().next().getZdbID());
        assertThat(dbLinkList.size(), greaterThan(0));
        assertThat(dbLinkList.size(), lessThan(3));
    }

    @Test
    public void getDBLinksForSecondRelatedMarker(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        Collection<MarkerDBLink> dbLinkList = sequenceRepository.getDBLinksForSecondRelatedMarker(m
                ,DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE
                ,MarkerRelationship.Type.CLONE_CONTAINS_GENE
        );
//        assertEquals(1, dbLinkList.size());
//        assertEquals("ZDB-DBLINK-050218-1177",dbLinkList.iterator().next().getZdbID());
        assertThat(dbLinkList.size(), greaterThan(0));
        assertThat(dbLinkList.size(), lessThan(3));
    }

    @Test
    public void getDBLinkAccessionsForMarkerOnly(){
        Marker marker = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        Set<String> accessionNumbers = new HashSet<String>(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
        assertThat(accessionNumbers.size(),greaterThan(2));
        assertThat(accessionNumbers.size(),lessThan(7));

        marker = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-030131-1815");
        accessionNumbers = new HashSet<String>(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
        assertThat(accessionNumbers.size(),equalTo(0));
    }

    @Test
    public void getDBLinkAccessionsForMarkerAndEncoding(){
        Marker marker = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-030131-1815");
        Set<String> accessionNumbers = new HashSet<String>(sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA));
        assertThat(accessionNumbers.size(),greaterThan(1));
        assertThat(accessionNumbers.size(),lessThan(3));

        marker = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-990415-8");
        accessionNumbers = new HashSet<String>(sequenceRepository.getDBLinkAccessionsForEncodedMarkers(marker, ForeignDBDataType.DataType.RNA));
        accessionNumbers.addAll(sequenceRepository.getDBLinkAccessionsForMarker(marker, ForeignDBDataType.DataType.RNA));
        assertThat(accessionNumbers.size(),greaterThan(15));
        assertThat(accessionNumbers.size(),lessThan(17));
    }

    @Test
    public void getGeoCandidatesLight(){
        Map<String,String> geoMap = sequenceRepository.getGeoAccessionCandidates();
        assertNotNull(geoMap);
        logger.info(geoMap.size());
        assertThat(geoMap.size(), greaterThan(50000));
        assertThat(geoMap.size(),  lessThan(500000));
        assertTrue(geoMap.containsKey("AF086761"));
        assertTrue(geoMap.containsValue("ZDB-EST-010427-5"));
        assertTrue(geoMap.containsKey("BI878777"));
        assertTrue(geoMap.containsValue("ZDB-GENE-110207-1"));
    }
}


