package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.gwt.ExpressionSmokeTest;
import org.zfin.marker.webservice.MarkerWebServiceTest;
import org.zfin.uniquery.smoketest.SiteSearchSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AnatomySmokeTest.class,
        ExpressionSmokeTest.class,
        AntibodySmokeTest.class,
        MarkerWebServiceTest.class,
        SiteSearchSmokeTest.class
})
public class ProductionSmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(ProductionSmokeTests.class);
    }

}
