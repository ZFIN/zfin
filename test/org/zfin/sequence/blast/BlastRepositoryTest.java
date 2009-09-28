package org.zfin.sequence.blast;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import org.zfin.sequence.blast.repository.BlastRepository;
import org.zfin.sequence.blast.presentation.BlastPresentationService;
import org.zfin.sequence.blast.presentation.DatabasePresentationBean;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.hibernate.SessionFactory;
import org.apache.log4j.Logger;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Tests blast repository methods.
 */
public class BlastRepositoryTest {

    private final static Logger logger = Logger.getLogger(BlastRepositoryTest.class) ;
    private final BlastRepository blastRepository = RepositoryFactory.getBlastRepository() ;

    static{
        SessionFactory sessionFactory=HibernateUtil.getSessionFactory();

        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @Test
    public void getSingleBlastRepository(){
        HibernateUtil.currentSession() ;
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES) ;
    }

    @Test
    public void getBlastDatabases(){
        HibernateUtil.currentSession() ;
        List<Database> databases;
        databases = blastRepository.getDatabases(Database.Type.NUCLEOTIDE,true,true) ;
        assertNotNull(databases) ;
        assertTrue(databases.size()>0) ;
        databases = blastRepository.getDatabases(Database.Type.PROTEIN,true,true) ;
        assertNotNull(databases) ;
        assertTrue(databases.size()>0) ;
    }

    @Test
    public void processingOnRealRootDatabases(){
        List<Database> proteinDatabases = RepositoryFactory.getBlastRepository().getDatabases(Database.Type.PROTEIN,true,true) ;
        assertNotNull(proteinDatabases) ;
        assertTrue(proteinDatabases.size()>0) ;
        List<DatabasePresentationBean> presentationBeans =  BlastPresentationService.orderDatabasesFromRoot(proteinDatabases) ;
        assertEquals(proteinDatabases.size(),presentationBeans.size());
    }

    @Test
    public void processingOnRealDatabases(){
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.ZFIN_MRPH) ;
        List<DatabasePresentationBean> presentationBeans =  BlastPresentationService.processFromChild(database,true) ;
        assertNotNull(presentationBeans);
        assertTrue(presentationBeans.size()>0);
    }

    @Test
    public void databaseByOriginationType(){
        List<Database> curatedDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED) ;
        assertNotNull(curatedDatabases) ;
        assertTrue(curatedDatabases.size()>0) ;

        List<Database> loadedDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.LOADED) ;
        assertNotNull(loadedDatabases) ;
        assertTrue(loadedDatabases.size()>0) ;

        List<Database> bothDatabases = RepositoryFactory.getBlastRepository().getDatabaseByOrigination(Origination.Type.CURATED,Origination.Type.LOADED) ;
        assertNotNull(bothDatabases) ;
        assertTrue(bothDatabases.size()>0) ;

        assertEquals( curatedDatabases.size()+loadedDatabases.size(),bothDatabases.size());
    }


    /**
     * A leaf should only contain itself.
     */
    @Test
    public void findAllLeavesForLeaf(){
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.VEGA_ZFIN) ;
        List<Database> databases = null ;
        try {
            databases = BlastPresentationService.getLeaves(database) ;
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }
        assertNotNull("should be not null",databases);
        assertEquals("should be one",databases.size(),1);
        assertEquals("should be self",database.getAbbrev(),databases.get(0).getAbbrev());
    }

    /**
     * A leaf should contain leaves in an ordered fashion.
     */
    @Test
    public void findAllLeavesForSmallOne(){
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES) ;
        List<Database> databaseLeaves = null ;
        List<Database> databaseChildren= null ;
        try {
            databaseChildren = BlastPresentationService.getDirectChildren(database) ;
            databaseLeaves = BlastPresentationService.getLeaves(database) ;
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }
        assertNotNull("should be not null",databaseLeaves);
        assertNotNull("should be not null",databaseChildren);
        assertEquals("database children",4,databaseChildren.size());
        assertEquals("database leaves",5,databaseLeaves.size());

    }

    /**
     * A leaf should contain leaves in an ordered fashion from sub-generated, as well.
     */
    @Test
    public void findAllLeavesForLots(){
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES) ;
        List<Database> databaseLeaves = null ;
        List<Database> databaseChildren= null ;
        try {
            databaseChildren = BlastPresentationService.getDirectChildren(database) ;
            databaseLeaves = BlastPresentationService.getLeaves(database) ;
            logger.info("Database Leaves here: " + databaseLeaves.get(0));
        } catch (BlastDatabaseException e) {
            fail(e.toString()) ;
        }
        assertNotNull("should be not null",databaseLeaves);
        assertNotNull("should be not null",databaseChildren);
        assertEquals("database children",4,databaseChildren.size());
        assertEquals("database leaves",5,databaseLeaves.size());
        //assertEquals("should be zfin_cdna_seq",Database.AvailableAbbrev.ZFIN_CDNA_SEQ,databaseLeaves.get(0).getAbbrev());

        //assertEquals("should be ZFINGenesWithExpression",Database.AvailableAbbrev.ZFINGENESWITHEXPRESSION,databaseLeaves.get(1).getAbbrev());
    }

    /**
     * A leaf should contain leaves in an ordered fashion from sub-generated, as well.
     */
    @Test
    public void getPublicParents(){

        // should go up once in the tree
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE) ;
        Database databaseParent = BlastPresentationService.getFirstPublicParentDatabase(database) ;
        assertNotNull(databaseParent);
        assertEquals(Database.AvailableAbbrev.ZFIN_MIRNA_MATURE,databaseParent.getAbbrev());

        // should go up twice in the tree
        Database databaseB= blastRepository.getDatabase(Database.AvailableAbbrev.VEGA_ZFIN) ;
        Database databaseParentB = BlastPresentationService.getFirstPublicParentDatabase(databaseB) ;
        assertNotNull(databaseParentB);
        assertEquals(Database.AvailableAbbrev.RNASEQUENCES,databaseParentB.getAbbrev());

        // should find itself in the tree
        Database databaseC= blastRepository.getDatabase(Database.AvailableAbbrev.RNASEQUENCES) ;
        Database databaseParentC = BlastPresentationService.getFirstPublicParentDatabase(databaseC) ;
        assertNotNull(databaseParentC);
        assertEquals(Database.AvailableAbbrev.RNASEQUENCES,databaseParentC.getAbbrev());

        // no tree, but should find self
        Database databaseD= blastRepository.getDatabase(Database.AvailableAbbrev.GENOMICDNA) ;
        Database databaseParentD = BlastPresentationService.getFirstPublicParentDatabase(databaseD) ;
        assertNotNull(databaseParentD);
        assertEquals(Database.AvailableAbbrev.GENOMICDNA,databaseParentD.getAbbrev());

        // should find a null
        Database databaseE= blastRepository.getDatabase(Database.AvailableAbbrev.REPBASE_ZF) ;
        Database databaseParentE = BlastPresentationService.getFirstPublicParentDatabase(databaseE) ;
        assertNull(databaseParentE);
    }

    @Test
    public void getValidAccessionNumberCount(){
        // should go up once in the tree
        Database database= blastRepository.getDatabase(Database.AvailableAbbrev.LOADEDMICRORNAMATURE) ;
        Integer count = RepositoryFactory.getBlastRepository().getNumberValidAccessionNumbers(database) ;
        logger.info("count: "+ count);
        assertTrue(count >0);
    }
}
