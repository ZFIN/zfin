package org.zfin.sequence ; 

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull ;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.zfin.TestConfiguration;
import org.zfin.marker.Marker;
import org.zfin.orthology.Species;
import org.zfin.orthology.repository.OrthologyRepository;

import org.zfin.sequence.repository.SequenceRepository  ; 
import org.zfin.sequence.repository.HibernateSequenceRepository  ;
import org.zfin.sequence.Accession ;

import org.hibernate.*;


import org.zfin.framework.HibernateSessionCreator ;
import org.zfin.framework.HibernateUtil ;
import org.zfin.framework.ExceptionFormatter;
import org.zfin.repository.RepositoryFactory ;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;

/**
 *  Class SequenceRepositoryTest.
 */

public class SequenceRepositoryTest {

    Logger logger = Logger.getLogger(SequenceRepositoryTest.class) ;

    private static SessionFactory sessionFactory ;
    private static SequenceRepository repository ; 

    static{
        if(repository==null){
            repository = new HibernateSequenceRepository() ;
        }

        sessionFactory = HibernateUtil.getSessionFactory() ; 

        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        // TODO: this should load a specific database instance for testing purposes
         
    }

    @After
    public void closeSession(){
        HibernateUtil.closeSession();
    }


    @Test
    public void testAccessionEntity(){
        Session session = HibernateUtil.currentSession();
        String hsqlString = "from Accession" ; 
        Query query = session.createQuery(hsqlString) ;
        query.setMaxResults(1) ;
        Accession accession =  (Accession) query.uniqueResult() ; 
        assertNotNull("database contains at least one accession", accession) ; 
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
        ForeignDB foreignDB = sr.getForeignDBByName("GenBank");
        ReferenceDatabase refDb = sr.getReferenceDatabaseByAlternateKey(
                foreignDB,
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetReferenceDatabase(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ReferenceDatabase refDb = sr.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK.toString(),
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
    }

    @Test
    public void testGetForeignDBByName(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ForeignDB foreignDB = sr.getForeignDBByName("GenBank");
        assertTrue("ForeignDB name is:Genbank",foreignDB.getDbName().equals("GenBank"));
    }

    //this test will only work when the data is not reloaded;
    //Todo: how do we test this method?  Maybe create fake data?
    @Test
    public void testGetAccessionByAlternateKey(){
        SequenceRepository sr = RepositoryFactory.getSequenceRepository();
        ForeignDB foreignDB = sr.getForeignDBByName("GenBank");
        ReferenceDatabase refDb = sr.getReferenceDatabaseByAlternateKey(
                foreignDB,
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);
        Accession accession = sr.getAccessionByAlternateKey("AL734309",refDb);
        assertTrue("ReferenceDatabase ZDBid is ZDB-FDBCONT-040412-36",refDb.getZdbID().equals("ZDB-FDBCONT-040412-36"));
        //assertTrue("Accession id is 732: ", accession.getID().equals("732"));
    }





} 


