package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.gwt.ExpressionSmokeTest;
import org.zfin.gwt.SimpleSmokeTest;
import org.zfin.gwt.lookup.LookupSmokeTest;
import org.zfin.gwt.marker.GeneEditSmokeTest;
import org.zfin.uniquery.smoketest.SiteSearchSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        MarkerViewSmokeTest.class,
        SimpleSmokeTest.class,
        AnatomySmokeTest.class,
        LookupSmokeTest.class,
//        AntibodyEditSmokeTest.class // speed issues on embryonix make this unstable
        ExpressionSmokeTest.class,
        AntibodySmokeTest.class,
        GeneEditSmokeTest.class,
        SiteSearchSmokeTest.class
})
public class SmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SmokeTests.class);
    }

}
