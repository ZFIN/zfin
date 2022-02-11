package org.zfin.infrastructure;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *     TEST fetching RecordAttributions of the 3 types that exist in the DB (LAB,PERS,PUB--not counting null)
 *         select *, get_obj_type ( recattrib_source_zdb_id ) as rectype
 *         FROM
 *                 record_attribution
 *         WHERE
 *         recattrib_data_zdb_id IN ( 'ZDB-EST-040130-186', 'ZDB-EST-010914-5', 'ZDB-EST-000426-244' )
 */

public class RecordAttributionTest extends AbstractDatabaseTest {

    @Test
    public void getRecordAttributionsOfLabType() {
        ActiveData activeData = new ActiveData();
        activeData.setZdbID("ZDB-EST-040130-186");
        try {
            List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        } catch (Exception e) {
            fail("should not catch exception fetching RecordAttribution records");
        }
    }

    @Test
    public void getRecordAttributionsOfPubType() {
        ActiveData activeData = new ActiveData();
        activeData.setZdbID("ZDB-EST-010914-5");
        List<RecordAttribution> recordAttributions = null;
        try {
            recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        } catch (Exception e) {
            fail("should not catch exception fetching RecordAttribution records");
        }
        assertNotNull(recordAttributions);
        RecordAttribution first = recordAttributions.get(0);
        assertEquals("ZDB-EST-010914-5 ZDB-PUB-010810-1 standard", first.toString());
    }

    @Test
    public void getRecordAttributionsOfPersonType() {
        ActiveData activeData = new ActiveData();
        activeData.setZdbID("ZDB-EST-000426-244");
        try {
            List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        } catch (Exception e) {
            fail("should not catch exception fetching RecordAttribution records");
        }
    }

}
