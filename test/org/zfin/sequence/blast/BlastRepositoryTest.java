package org.zfin.sequence.blast;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.blast.presentation.BlastPresentationService;
import org.zfin.sequence.blast.presentation.DatabasePresentationBean;
import org.zfin.sequence.blast.repository.BlastRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getSequenceRepository;

/**
 * Tests blast repository methods.
 */
public class BlastRepositoryTest extends AbstractDatabaseTest {

    private final static Logger logger = LogManager.getLogger(BlastRepositoryTest.class);
    private final BlastRepository blastRepository = RepositoryFactory.getBlastRepository();


    @Test
    public void getSingleBlastRepository() {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES);
        assertNotNull(database);
        database = blastRepository.getDatabase(Database.AvailableAbbrev.MEGA_BLAST);
        assertNotNull(database);
    }

    @Test
    public void getBlastDatabases() {
        HibernateUtil.currentSession();
        List<Database> databases;
        databases = blastRepository.getDatabases(Database.Type.NUCLEOTIDE, true, true);
        assertNotNull(databases);
        assertTrue(databases.size() > 0);
        databases = blastRepository.getDatabases(Database.Type.PROTEIN, true, true);
        assertNotNull(databases);
        assertTrue(databases.size() > 0);
    }

    @Test
    public void processingOnRealRootDatabases() {
        List<Database> proteinDatabases = RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN, true, true);
        assertNotNull(proteinDatabases);
        assertTrue(proteinDatabases.size() > 0);
        List<DatabasePresentationBean> presentationBeans = BlastPresentationService.orderDatabasesFromRoot(proteinDatabases);
        assertEquals(proteinDatabases.size(), presentationBeans.size());
    }

    @Test
    public void processingOnRealDatabases() {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.ZFIN_MRPH);
        List<DatabasePresentationBean> presentationBeans = BlastPresentationService.processFromChild(database, true);
        assertNotNull(presentationBeans);
        assertTrue(presentationBeans.size() > 0);
    }

    @Test
    public void databaseByOriginationType() {
        List<Database> curatedDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED);
        assertNotNull(curatedDatabases);
        assertTrue(curatedDatabases.size() > 0);

        List<Database> loadedDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.LOADED);
        assertNotNull(loadedDatabases);
        assertTrue(loadedDatabases.size() > 0);

        List<Database> bothDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED);
        assertNotNull(bothDatabases);
        assertTrue(bothDatabases.size() > 0);

        assertEquals(curatedDatabases.size() + loadedDatabases.size(), bothDatabases.size());
    }


    /**
     * A leaf should only contain itself.
     */
    @Test
    public void findAllLeavesForLeaf() throws BlastDatabaseException {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.VEGA_ZFIN);
        List<Database> databases = null;
        databases = BlastPresentationService.getLeaves(database);
        assertNotNull("should be not null", databases);
        assertEquals("should be one", databases.size(), 1);
        assertEquals("should be self", database.getAbbrev(), databases.get(0).getAbbrev());
    }

    /**
     * A leaf should contain leaves in an ordered fashion.
     */
    @Test
    public void findAllLeavesForSmallOne() throws BlastDatabaseException {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES);
        List<Database> databaseLeaves = BlastPresentationService.getLeaves(database);
        List<Database> databaseChildren = BlastPresentationService.getDirectChildren(database);
        assertNotNull("should be not null", databaseLeaves);
        assertNotNull("should be not null", databaseChildren);
        assertTrue("database children", databaseChildren.size() > 3);
        assertTrue("database leaves", databaseLeaves.size() > 4);

    }

    /**
     * A leaf should contain leaves in an ordered fashion from sub-generated, as well.
     */
    @Test
    public void findAllLeavesForLots() throws BlastDatabaseException {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES);
        List<Database> databaseLeaves = BlastPresentationService.getLeaves(database);
        List<Database> databaseChildren = BlastPresentationService.getDirectChildren(database);
        logger.info("Database Leaves here: " + databaseLeaves.get(0));

        assertNotNull("should be not null", databaseLeaves);
        assertNotNull("should be not null", databaseChildren);
        assertTrue("database children", databaseChildren.size() >= 4);
        assertTrue("database leaves", databaseLeaves.size() >= 5);
        //assertEquals("should be zfin_cdna_seq",Database.AvailableAbbrev.ZFIN_CDNA_SEQ,databaseLeaves.get(0).getAbbrev());

        //assertEquals("should be ZFINGenesWithExpression",Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION,databaseLeaves.get(1).getAbbrev());
    }

    /**
     * A leaf should contain leaves in an ordered fashion from sub-generated, as well.
     */
    @Test
    public void getPublicParents() {

        // should go up once in the tree
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE);
        Database databaseParent = BlastPresentationService.getFirstPublicParentDatabase(database);
        assertNotNull(databaseParent);
        assertEquals(Database.AvailableAbbrev.ZFIN_MIRNA_MATURE, databaseParent.getAbbrev());

        // should go up twice in the tree
        Database databaseB = blastRepository.getDatabase(Database.AvailableAbbrev.VEGA_ZFIN);
        Database databaseParentB = BlastPresentationService.getFirstPublicParentDatabase(databaseB);
        assertNotNull(databaseParentB);
        assertEquals(Database.AvailableAbbrev.RNASEQUENCES, databaseParentB.getAbbrev());

        // should find itself in the tree
        Database databaseC = blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES);
        Database databaseParentC = BlastPresentationService.getFirstPublicParentDatabase(databaseC);
        assertNotNull(databaseParentC);
        assertEquals(Database.AvailableAbbrev.RNASEQUENCES, databaseParentC.getAbbrev());

        // no tree, but should find self
        Database databaseD = blastRepository.getDatabase(Database.AvailableAbbrev.GENOMICDNA);
        Database databaseParentD = BlastPresentationService.getFirstPublicParentDatabase(databaseD);
        assertNotNull(databaseParentD);
        assertEquals(Database.AvailableAbbrev.GENOMICDNA, databaseParentD.getAbbrev());

        // should find a null
        Database databaseE = blastRepository.getDatabase(Database.AvailableAbbrev.REPBASE_ZF);
        Database databaseParentE = BlastPresentationService.getFirstPublicParentDatabase(databaseE);
        assertNull(databaseParentE);
    }

    @Test
    public void getValidAccessionNumberCount() {
        // should go up once in the tree
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE);
        Integer count = RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database);
        logger.info("count: " + count);
        assertTrue(count > 0);
    }

    @Test
    public void handlePreviousAccessions() {
        Database database = blastRepository.getDatabase(Database.AvailableAbbrev.CURATEDMICRORNAMATURE);
        Set<String> validAccessions = blastRepository.getAllValidAccessionNumbers(database);
        assertTrue(validAccessions.size() > 0);
        List<String> previousAccessions = blastRepository.getPreviousAccessionsForDatabase(database);
        assertTrue(previousAccessions.size() > 0);
        int previousAccessionSize = previousAccessions.size();
        List<String> accessionsToAdd = new ArrayList<String>();
        accessionsToAdd.add("A");
        accessionsToAdd.add("B");
        accessionsToAdd.add("C");

        blastRepository.addPreviousAccessions(database, accessionsToAdd);
        assertEquals(previousAccessionSize + accessionsToAdd.size(), blastRepository.getPreviousAccessionsForDatabase(database).size());

        List<String> accessionsToRemove = new ArrayList<String>();
        accessionsToRemove.add("A");
        accessionsToRemove.add("B");
        blastRepository.removePreviousAccessions(database, accessionsToRemove);
        List<String> previosAcStringList = blastRepository.getPreviousAccessionsForDatabase(database);
        assertEquals(previousAccessionSize + accessionsToAdd.size() - accessionsToRemove.size(), previosAcStringList.size());
        MountedWublastBlastService.getInstance().updatePreviousAccessions(database, validAccessions, blastRepository.getPreviousAccessionsForDatabase(database));
        assertTrue(CollectionUtils.isEqualCollection(validAccessions, blastRepository.getPreviousAccessionsForDatabase(database)));
    }

    @Test
    public void blastDatabases() {

        List<DBLink> dbLinkGenBank = getSequenceRepository().getDBLinks(ForeignDB.AvailableName.UNIPROTKB, 5);
        assertNotNull(dbLinkGenBank);
        List<DBLink> dbLinkList = getSequenceRepository().getDBLinksForAccession(dbLinkGenBank.get(0).getAccessionNumber());
        assertThat(dbLinkList.size(), greaterThan(0));
        assertThat(dbLinkList.size(), lessThan(10));
        for (DBLink dbLink : dbLinkList) {
            if (dbLink.getReferenceDatabase().getForeignDB().getDbName().equals(ForeignDB.AvailableName.UNIPROTKB)) {
                List<Database> blastDatabases = dbLink.getBlastableDatabases();
                assertEquals(3, blastDatabases.size());
                for (Database database : blastDatabases) {
                    assertTrue(
                            database.getAbbrev() == Database.AvailableAbbrev.SPTR_ZF
                                    ||
                                    database.getAbbrev() == Database.AvailableAbbrev.PBLAST
                                    ||
                                    database.getAbbrev() == Database.AvailableAbbrev.ENSEMBL_P
                    );
                }


            }
        }
    }

    @Test
    public void blastDatabasesProteins() {
        List<DBLink> dbLinkList = getSequenceRepository().getDBLinksForAccession("NP_001071049");
        for (DBLink dbLink : dbLinkList) {
            if (dbLink.getReferenceDatabase().getForeignDB().getDbName().equals(ForeignDB.AvailableName.REFSEQ)) {
                dbLink.getBlastableDatabases();

            }
        }
    }

    @Test
    public void testGetValidAccessionCountsForAllBlastDatabases() {
        Map<String, Integer> map = blastRepository.getValidAccessionCountsForAllBlastDatabases();
        assertNotNull("accession count map is not null", map);

        List<Database> databases = blastRepository.getDatabaseByOrigination(Origination.Type.CURATED, Origination.Type.LOADED, Origination.Type.MARKERSEQUENCE);

        for (String key : map.keySet()) {
            assertNotNull(key + " has a non-null count", map.get(key));
        }

        for (Database database : databases) {
            assertNotNull(database.getAbbrev().toString() + " should be in the accession count map", map.get(database.getAbbrev().toString()));
        }
    }

    @Test
    public void testEnsemblGenes() {
        List<MarkerDBLink> map = getSequenceRepository().getAllEnsemblGenes(ForeignDB.AvailableName.ENSEMBL_GRCZ11_);
        assertNotNull("accession count map is not null", map);
    }

    /**
     * Tests that Database entity can be fetched directly via HQL.
     * Verifies JPA annotation mapping is correct.
     */
    @Test
    public void testDatabaseEntityFetch() {
        List<Database> databases = HibernateUtil.currentSession()
                .createQuery("FROM Database", Database.class)
                .setMaxResults(5)
                .list();
        assertNotNull(databases);
        assertFalse("Should have at least one database", databases.isEmpty());

        Database db = databases.get(0);
        assertNotNull("Database should have zdbID", db.getZdbID());
        assertNotNull("Database should have name", db.getName());
        assertNotNull("Database should have abbrev", db.getAbbrev());
        assertNotNull("Database should have type", db.getType());
    }

    /**
     * Tests that Origination entity can be fetched directly via HQL.
     * Verifies JPA annotation mapping is correct.
     */
    @Test
    public void testOriginationEntityFetch() {
        List<Origination> originations = HibernateUtil.currentSession()
                .createQuery("FROM Origination", Origination.class)
                .setMaxResults(5)
                .list();
        assertNotNull(originations);
        assertFalse("Should have at least one origination", originations.isEmpty());

        Origination orig = originations.get(0);
        assertNotNull("Origination should have id", orig.getId());
        assertNotNull("Origination should have type", orig.getType());
    }

    /**
     * Tests that DatabaseRelationship entity can be fetched directly via HQL.
     * Verifies JPA annotation mapping is correct.
     */
    @Test
    public void testDatabaseRelationshipEntityFetch() {
        List<DatabaseRelationship> relationships = HibernateUtil.currentSession()
                .createQuery("FROM DatabaseRelationship", DatabaseRelationship.class)
                .setMaxResults(5)
                .list();
        assertNotNull(relationships);
        assertFalse("Should have at least one relationship", relationships.isEmpty());

        DatabaseRelationship rel = relationships.get(0);
        assertNotNull("Relationship should have id", rel.getId());
        assertNotNull("Relationship should have order", rel.getOrder());
        assertNotNull("Relationship should have child", rel.getChild());
    }

    /**
     * Tests that BlastRegenerationCache entity can be fetched directly via HQL.
     * Verifies JPA annotation mapping is correct.
     */
    @Test
    public void testBlastRegenerationCacheEntityFetch() {
        List<BlastRegenerationCache> caches = HibernateUtil.currentSession()
                .createQuery("FROM BlastRegenerationCache", BlastRegenerationCache.class)
                .setMaxResults(5)
                .list();
        assertNotNull(caches);
        // This table may be empty, so just verify the query works
        if (!caches.isEmpty()) {
            BlastRegenerationCache cache = caches.get(0);
            assertNotNull("Cache should have id", cache.getId());
            assertNotNull("Cache should have accession", cache.getAccession());
            assertNotNull("Cache should have blastDatabase", cache.getBlastDatabase());
        }
    }

}
