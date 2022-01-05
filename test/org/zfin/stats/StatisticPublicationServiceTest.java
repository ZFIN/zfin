package org.zfin.stats;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;

import static org.junit.Assert.assertNotNull;

public class StatisticPublicationServiceTest extends AbstractDatabaseTest {

    @Test
    public void getAllPublicationAntibodies() {
        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> rows = service.getAllPublicationAntibodies(new Pagination());
        assertNotNull(rows);
    }
}
