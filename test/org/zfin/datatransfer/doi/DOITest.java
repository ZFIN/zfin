package org.zfin.datatransfer.doi;

import org.hibernate.SessionFactory;
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


    @Test
    public void testDOIConnectivity(){
        HibernateUtil.createTransaction();
        Citexplore citexplore = new Citexplore();
        List<Publication> pubs = new ArrayList<Publication>();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Publication pub1 = publicationRepository.getPublication("ZDB-PUB-090324-13") ;
        Publication pub2 = publicationRepository.getPublication("ZDB-PUB-090526-18") ;
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
