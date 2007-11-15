package org.zfin.sequence.blast ; 

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import org.zfin.TestConfiguration;

import org.zfin.sequence.blast.repository.BlastRepository  ; 
import org.zfin.sequence.blast.repository.HibernateBlastRepository  ; 

import org.hibernate.SessionFactory ;
import org.hibernate.Session;



import org.zfin.framework.HibernateSessionCreator ; 
import org.zfin.framework.HibernateUtil ; 

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
        String hsqlString = "from org.zfin.sequence.blast.Hit" ; 
        org.hibernate.Query query = session.createQuery(hsqlString) ;
        query.setMaxResults(1) ;
        Hit hit =  (Hit) query.uniqueResult() ; 
        assertNotNull("database contains at least one blast hit",hit  ) ; 
    }

    @Test
    public void testQueryEntity(){
        Session session = HibernateUtil.currentSession();
        String hsqlString = "from Query" ; 
        org.hibernate.Query query = session.createQuery(hsqlString) ;
        query.setMaxResults(1) ;
        org.zfin.sequence.blast.Query queryEntity =  (org.zfin.sequence.blast.Query) query.uniqueResult() ; 
        assertNotNull("database contains at least one blast query",queryEntity  ) ; 
    }



} 


