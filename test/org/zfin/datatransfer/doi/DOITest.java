package org.zfin.datatransfer.doi;

import org.hibernate.Criteria;
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
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
}
