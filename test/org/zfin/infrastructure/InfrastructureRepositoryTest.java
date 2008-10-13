package org.zfin.infrastructure;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.repository.RepositoryFactory;
import org.zfin.marker.MarkerType;
import org.zfin.marker.Marker;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.HibernateInfrastructureRepository;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.util.List;

/**
 * Class InfrastructureRepositoryTest.
 */

public class InfrastructureRepositoryTest {

    private static InfrastructureRepository repository;
    private static Session session;

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
        session = HibernateUtil.currentSession();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    //@Test
    public void persistActiveData() {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            String testZdbID = "ZDB-GENE-123";
            ActiveData testActiveData = repository.getActiveData(testZdbID);
            assertNull("ActiveData not found prior to insert", testActiveData);
            repository.insertActiveData(testZdbID);
            testActiveData = repository.getActiveData(testZdbID);
            assertNotNull("ActiveData found after insert", testActiveData);
            repository.deleteActiveData(testActiveData);
            testActiveData = repository.getActiveData(testZdbID);
            assertNull("ActiveData found after delete", testActiveData);
        } catch (HibernateException e) {
            fail("failed");
            e.printStackTrace();
        } finally {
            tx.rollback();
        }

    }

    @Test
    public void persistRecordAttribution() {

        try {
            session.beginTransaction();
            String dataZdbID = "ZDB-DALIAS-uuiouy";
            String sourceZdbID = "ZDB-PUB-89087";
            repository.insertActiveData(dataZdbID);
            repository.insertActiveSource(sourceZdbID);
            RecordAttribution attribute = repository.getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNull("RecordAttribution not found prior to insert", attribute);
            repository.insertRecordAttribution(dataZdbID, sourceZdbID);
            attribute = repository.getRecordAttribution(dataZdbID, sourceZdbID, null);
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
    }

    @Test
    public void allMapNames(){
        String string = "pdx";
        List<AllNamesFastSearch> all = repository.getAllNameMarkerMatches(string);
        assertTrue(all != null);
    }

    @Test
    public void allMapNamesGenes(){
        String string = "pdx";
        MarkerType type = RepositoryFactory.getMarkerRepository().getMarkerTypeByName(Marker.Type.GENE.toString());
        List<AllMarkerNamesFastSearch> all = repository.getAllNameMarkerMatches(string, type);
        assertTrue(all != null);
    }
    @Test
    public void replacementZDB(){
        String replacedZdbID = "ZDB-ANAT-010921-497";
        ReplacementZdbID replacementZdbID = repository.getReplacementZdbId(replacedZdbID);
        assertTrue(replacementZdbID != null);

        assertEquals("ZDB-ANAT-011113-37", replacementZdbID.getReplacementZdbID());
    }

    @Test
    public void dataAliasAbbrev(){
        String name = "acerebellar";
        List<String> list= repository.getDataAliasesWithAbbreviation(name);
        assertTrue(list != null);
        assertTrue(list.size() == 1);
        assertEquals("fgf8a", list.get(0));
    }

    @Test
    public void anatomyTokens(){
        String name = "presumptive";
        List<String> list= repository.getAnatomyTokens(name);
        assertTrue(list != null);
        assertTrue(list.size() > 10);
    }
}


