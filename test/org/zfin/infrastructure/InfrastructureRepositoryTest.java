package org.zfin.infrastructure;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.HibernateInfrastructureRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;

/**
 * Class InfrastructureRepositoryTest.
 */

public class InfrastructureRepositoryTest {

    private static InfrastructureRepository repository;

    static {
        if (repository == null) {
            repository = new HibernateInfrastructureRepository();
        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        Session session = HibernateUtil.currentSession();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    public void persistActiveData() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();

        String testZdbID = "ZDB-ABC-123";
        ActiveData testActiveData = null;
        testActiveData = repository.getActiveData(testZdbID);
        assertNull("ActiveData not found prior to insert", testActiveData);
        repository.insertActiveData(testZdbID);
        testActiveData = repository.getActiveData(testZdbID);
        assertNotNull("ActiveData found after insert", testActiveData);
        repository.deleteActiveData(testActiveData);
        testActiveData = repository.getActiveData(testZdbID);
        assertNull("ActiveData found after delete", testActiveData);

        session.getTransaction().rollback();
    }

    @Test
    public void persistRecordAttribution() {

        Session session = HibernateUtil.currentSession();
        try {
            session.beginTransaction();
            String dataZdbID = "ZDB-DALIAS-uuiouy";
            String sourceZdbID = "ZDB-PUB-89087";
            repository.insertActiveData(dataZdbID);
            repository.insertActiveSource(sourceZdbID);
            RecordAttribution attribute = repository.getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNull("RecordAttribution not found prior to insert", attribute);
            repository.insertRecordAttribution(dataZdbID, sourceZdbID);
            attribute = repository.getRecordAttribution(dataZdbID, sourceZdbID,null);
            assertNotNull("RecordAttribution found after insert", attribute);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }

/*
        repository.deleteRecordAttribution( testRecordAttribution ) ;
        testRecordAttribution = repository.getRecordAttribution( testZdbID ) ;
        assertNull ("RecordAttribution found after delete", testRecordAttribution );
*/
    }


}


