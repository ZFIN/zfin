package org.zfin.sequence ;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.orthology.Species;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.repository.HibernateSequenceRepository;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.List;

import static org.junit.Assert.*;

/**
 *  Class SequenceRepositoryTest.
 */

public class SequenceRepositoryTest {

    private final static Logger logger = Logger.getLogger(SequenceRepositoryTest.class) ;

    private static SequenceRepository repository ;

    static{
        if(repository==null){
            repository = new HibernateSequenceRepository() ;
        }

        SessionFactory sessionFactory=HibernateUtil.getSessionFactory();

        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @After
    public void closeSession(){
        HibernateUtil.closeSession();
    }


    @Test
    public void testAccessionEntity(){
        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction()  ;
            Accession accession1 = new Accession() ;
            String number ="AC:TEST" ;
            String abbrev ="AC:TEST_ABBREV" ;
            accession1.setNumber(number);
            accession1.setAbbreviation(abbrev);
            ReferenceDatabase genBankRefDB =RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.GENOMIC,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);
            accession1.setReferenceDatabase(genBankRefDB);
            session.save(accession1) ;
            String hsqlString = "from Accession acc where acc.number = :number" ;
            Query query = session.createQuery(hsqlString) ;
            query.setString("number",number) ;
//            query.setMaxResults(1) ;
            Accession accession =  (Accession) query.uniqueResult() ;
            assertNotNull("database contains at least one accession", accession) ;
            assertEquals("abbrevs are equal", abbrev, accession.getAbbreviation());
        } finally {
            session.getTransaction().rollback();
        }
    }

    @Test
    public void testReferenceDatabaseEntity(){
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ReferenceDatabase.class);
        criteria.setMaxResults(1) ;
        ReferenceDatabase referenceDatabase = (ReferenceDatabase) criteria.uniqueResult();
        assertNotNull("database contains at least one reference database", referenceDatabase) ;
    }

    @Test
    public void testForeignDBEntity(){
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ForeignDB.class);
        criteria.setMaxResults(1) ;
        ForeignDB foreignDB = (ForeignDB) criteria.uniqueResult();
        assertNotNull("database contains at least one foreignDB ", foreignDB) ;
    }
    @Test
    public void testGetReferenceDatabaseByAlternateKey(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetReferenceDatabase(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetForeignDBByName(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ForeignDB foreignDB = sr.getForeignDBByName(ForeignDB.AvailableName.GENBANK);
        assertTrue("ForeignDB name is:Genbank",foreignDB.getDbName() == ForeignDB.AvailableName.GENBANK );
    }

    //this test will only work when the data is not reloaded;
    @Test
    public void testGetAccessionByAlternateKey(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void getReferenceDatabasesWithInternalBlast(){
        List<ReferenceDatabase> referenceDatabases = RepositoryFactory.getSequenceRepository().getReferenceDatabasesWithInternalBlast();
        assertTrue("should be at least 10 of these things", referenceDatabases.size()> 10) ;
        for(ReferenceDatabase referenceDatabase : referenceDatabases){
            assertNotNull("must have blast databases", referenceDatabase.getPrimaryBlastDatabase());
            Database database = referenceDatabase.getPrimaryBlastDatabase() ;
        }
    }

}


