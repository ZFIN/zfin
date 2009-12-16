package org.zfin;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.repository.PaginationResultFactory;

import static org.junit.Assert.assertEquals;

/**
 */
public class PaginationResultTest {

    private Session session ;

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }

    }


    @Before
    public void setUp() {
        TestConfiguration.configure();
        session = HibernateUtil.currentSession() ;

    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    public void paginationWithMax(){
        Criteria criteria = session.createCriteria(DevelopmentStage.class) ;
        criteria.addOrder(Order.asc("hoursEnd")) ;
        PaginationResult<DevelopmentStage> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(3,criteria.scroll()) ;
        assertEquals(45,paginationResult.getTotalCount()) ;
        assertEquals(3,paginationResult.getPopulatedResults().size()) ;
        assertEquals(paginationResult.getPopulatedResults().get(0).getName(), "Zygote:1-cell" );
        assertEquals(paginationResult.getPopulatedResults().get(2).getName(), "Cleavage:4-cell" );
    }


    @Test
    public void paginationWithFirstAndLast(){
        Criteria criteria = session.createCriteria(DevelopmentStage.class) ;
        criteria.addOrder(Order.asc("hoursEnd")) ;
        PaginationResult<DevelopmentStage> results1 = PaginationResultFactory.createResultFromScrollableResultAndClose(1,5,criteria.scroll()) ;
        assertEquals(45,results1.getTotalCount()) ;
        assertEquals(4,results1.getPopulatedResults().size()) ;
        assertEquals(results1.getPopulatedResults().get(0).getName(), "Cleavage:2-cell" );
        assertEquals(results1.getPopulatedResults().get(2).getName(), "Cleavage:8-cell" );

        PaginationResult<DevelopmentStage> results2 = PaginationResultFactory.createResultFromScrollableResultAndClose(0,3,criteria.scroll()) ;
        assertEquals(45,results2.getTotalCount()) ;
        assertEquals(3,results2.getPopulatedResults().size()) ;
        assertEquals(results2.getPopulatedResults().get(0).getName(), "Zygote:1-cell" );
        assertEquals(results2.getPopulatedResults().get(2).getName(), "Cleavage:4-cell" );
    }

}
