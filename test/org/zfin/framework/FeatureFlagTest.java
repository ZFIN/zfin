package org.zfin.framework;

import org.hibernate.Session;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.featureflag.FeatureFlag;
import org.zfin.framework.featureflag.FeatureFlagEnum;
import org.zfin.framework.featureflag.FeatureFlags;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 */
public class FeatureFlagTest extends AbstractDatabaseTest {

    /**
     * Is this the best way to keep DB in sync with enum?
     */
    @Test
    public void dbSyncTest(){
        Set<String> flagNames = Arrays.stream(FeatureFlagEnum.values())
                .map(item -> item.getName())
                .collect(Collectors.toSet());

        Session session = HibernateUtil.currentSession();
        org.hibernate.query.Query query = session.createQuery("from FeatureFlag");
        List<FeatureFlag> dbFlags = query.list();
        Set<String> dbFlagNames = dbFlags.stream().map(item -> item.getName()).collect(Collectors.toSet());

        assertEquals(flagNames, dbFlagNames);
    }
}
