package org.zfin.datatransfer.doi;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class DOITest {

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    /**
     * get 2 pubs with valid dois, set them to null and repopulate them using the service class
     */
    @Test
    public void testDOIConnectivity(){
        HibernateUtil.createTransaction();
        Citexplore citexplore = new Citexplore();
        List<Publication> pubs = new ArrayList<Publication>();
        Criteria crit = HibernateUtil.currentSession().createCriteria(Publication.class) ;
        crit.setMaxResults(2);
        crit.add(Restrictions.isNotNull("doi"));
        List<Publication> publications = (List<Publication>) crit.list();
        assertEquals(2,publications.size());
        Publication pub1 = publications.get(0);
        Publication pub2 = publications.get(1);
        pub1.setDoi(null);
        pubs.add(pub1) ;
        pub2.setDoi(null);
        pubs.add(pub2) ;
//        List<Publication> pubs = publicationRepository.getPublicationsWithAccessionButNoDOI(2) ;
        citexplore.getDoisForPubmedID(pubs) ;

        assertNotNull(pub1.getDoi()) ;
        assertNotNull(pub2.getDoi()) ;
        HibernateUtil.rollbackTransaction();

    }


    @Test
    public void getPublicationswithAccessionsButNoDOIAndFewAttempts(){

        try {
            HibernateUtil.createTransaction();

            List<Publication> publications ;
            int maxResults = 1 ;
            int maxAttempts = 3 ;
            HibernateUtil.currentSession().createQuery(" " +
            " update DOIAttempt da set da.numAttempts = 0 " +
            "").executeUpdate() ;
            publications = RepositoryFactory.getPublicationRepository().getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertNotNull(publications);
            assertEquals(maxResults,publications.size());
            String pubZdbID1 = publications.get(0).getZdbID() ;
            publications = RepositoryFactory.getPublicationRepository().getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertNotNull(publications);
            assertEquals(maxResults,publications.size());
            String pubZdbID2 = publications.get(0).getZdbID() ;
            assertEquals(pubZdbID1,pubZdbID2);

            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertNotNull(publications);
            assertEquals(maxResults,publications.size());
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertEquals(pubZdbID1,pubZdbID2);


            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertNotNull(publications);
            assertEquals(maxResults,publications.size());
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertEquals(pubZdbID1,pubZdbID2);

            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            HibernateUtil.currentSession().flush();
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertNotNull(publications);
            assertEquals(maxResults,publications.size());
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertFalse(pubZdbID1.equals(pubZdbID2));
        } catch (Exception e) {
            fail(e.toString()) ;
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

}
