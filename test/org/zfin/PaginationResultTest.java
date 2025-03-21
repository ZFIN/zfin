package org.zfin;

import org.hibernate.query.Query;
import org.junit.Test;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.repository.PaginationResultFactory;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class PaginationResultTest extends AbstractDatabaseTest {

    @Test
    public void paginationWithMax() {
        Query<DevelopmentStage> query = HibernateUtil.currentSession().createQuery("from DevelopmentStage order by hoursEnd asc", DevelopmentStage.class);
        PaginationResult<DevelopmentStage> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(3, query.scroll());
        assertEquals(45, paginationResult.getTotalCount());
        assertEquals(3, paginationResult.getPopulatedResults().size());
        assertEquals(paginationResult.getPopulatedResults().get(0).getName(), "Zygote:1-cell");
        assertEquals(paginationResult.getPopulatedResults().get(2).getName(), "Cleavage:4-cell");
    }


    @Test
    public void paginationWithFirstAndLast() {
        Query<DevelopmentStage> query = HibernateUtil.currentSession().createQuery("from DevelopmentStage order by hoursEnd asc", DevelopmentStage.class);
        PaginationResult<DevelopmentStage> results1 = PaginationResultFactory.createResultFromScrollableResultAndClose(1, 5, query.scroll());
        assertEquals(45, results1.getTotalCount());
        assertEquals(4, results1.getPopulatedResults().size());
        assertEquals(results1.getPopulatedResults().get(0).getName(), "Cleavage:2-cell");
        assertEquals(results1.getPopulatedResults().get(2).getName(), "Cleavage:8-cell");

        PaginationResult<DevelopmentStage> results2 = PaginationResultFactory.createResultFromScrollableResultAndClose(0, 3, query.scroll());
        assertEquals(45, results2.getTotalCount());
        assertEquals(3, results2.getPopulatedResults().size());
        assertEquals(results2.getPopulatedResults().get(0).getName(), "Zygote:1-cell");
        assertEquals(results2.getPopulatedResults().get(2).getName(), "Cleavage:4-cell");
    }

}
