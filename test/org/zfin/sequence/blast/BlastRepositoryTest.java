package org.zfin.sequence.blast ; 

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.zfin.TestConfiguration;

import org.zfin.sequence.blast.repository.BlastRepository  ; 
import org.zfin.sequence.blast.repository.HibernateBlastRepository  ;
import org.zfin.sequence.reno.Run;

import org.hibernate.SessionFactory ;
import org.hibernate.Session;
import org.hibernate.Criteria;


import org.zfin.framework.HibernateSessionCreator ;
import org.zfin.framework.HibernateUtil ;

import java.util.List;

/**
 *  Class BlastRepositoryTest.
 */
public class BlastRepositoryTest {

    private static BlastRepository repository ; 

    static{
        if(repository==null){
            repository = new HibernateBlastRepository() ;
        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory() ;

        if(sessionFactory == null){
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    @Test
    public void testHitEntity(){
        Session session = HibernateUtil.currentSession();
        Criteria runCrit = session.createCriteria(Run.class);
        Criteria hitCrit = session.createCriteria(Hit.class);

        List<Run> runs = runCrit.list();
        List<Hit> hits = hitCrit.list();

        if (runs.size() > 0) {
            assertTrue("If there is a run in the database, there should be some hits",  hits.size() > 0  );
        }


    }

    @Test
    public void testQueryEntity(){
        Session session = HibernateUtil.currentSession();

        Criteria runCrit = session.createCriteria(Run.class);
        Criteria queryCrit = session.createCriteria(Query.class);

        List<Run> runs = runCrit.list();
        List<Hit> queries = queryCrit.list();

        if (runs.size() > 0) {
            assertTrue("If there is a run in the database, there should be some queries",  queries.size() > 0  );
        }
        
    }



} 


