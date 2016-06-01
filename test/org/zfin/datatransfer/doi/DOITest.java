package org.zfin.datatransfer.doi;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 */
public class DOITest extends AbstractDatabaseTest{

    private Citexplore citexplore = new Citexplore();

    /**
     * get 2 pubs with valid dois, set them to null and repopulate them using the service class
     */
    @Test
    public void testDOIConnectivity(){
        HibernateUtil.createTransaction();
        try {
            List<Publication> pubs = new ArrayList<>();
            Criteria crit = HibernateUtil.currentSession().createCriteria(Publication.class);
            crit.setMaxResults(2);
            crit.add(Restrictions.isNotNull("doi"));
            List<Publication> publications = (List<Publication>) crit.list();
            assertThat(publications, hasSize(2));

            Publication pub1 = publications.get(0);
            Publication pub2 = publications.get(1);
            pub1.setDoi(null);
            pubs.add(pub1);
            pub2.setDoi(null);
            pubs.add(pub2);
            citexplore.getDoisForPubmedID(pubs);

            assertThat(pub1.getDoi(), notNullValue());
            assertThat(pub2.getDoi(), notNullValue());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

    }

    @Test
    public void testValidPub(){
        List<Publication> pubs = new ArrayList<>();
        Publication p = (Publication) HibernateUtil.currentSession().get(Publication.class,"ZDB-PUB-101122-23") ;
        pubs.add(p);
        pubs = citexplore.getDoisForPubmedID(pubs) ;
        assertThat(pubs, notNullValue());
        assertThat(pubs, not(empty()));
        assertThat(pubs.get(0).getDoi(), is("10.1095/biolreprod.110.086363"));
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
            assertThat(publications, notNullValue());
            assertThat(publications, hasSize(maxResults));

            String pubZdbID1 = publications.get(0).getZdbID() ;
            publications = RepositoryFactory.getPublicationRepository().getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertThat(publications, notNullValue());
            assertThat(publications, hasSize(maxResults));
            String pubZdbID2 = publications.get(0).getZdbID() ;
            assertThat(pubZdbID1, is(pubZdbID2));

            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertThat(publications, notNullValue());
            assertThat(publications, hasSize(maxResults));
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertThat(pubZdbID1, is(pubZdbID2));

            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertThat(publications, notNullValue());
            assertThat(publications, hasSize(maxResults));
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertThat(pubZdbID1, is(pubZdbID2));

            RepositoryFactory.getPublicationRepository().addDOIAttempts(publications) ;
            HibernateUtil.currentSession().flush();
            publications = RepositoryFactory.getPublicationRepository(). getPublicationsWithAccessionButNoDOIAndLessAttempts(maxAttempts,maxResults);
            assertThat(publications, notNullValue());
            assertThat(publications, hasSize(maxResults));
            pubZdbID2 = publications.get(0).getZdbID() ;
            assertThat(pubZdbID1, is(not(pubZdbID2)));
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

}
