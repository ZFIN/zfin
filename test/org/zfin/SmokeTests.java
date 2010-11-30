package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.feature.presentation.GenotypeDetailSmokeTest;
import org.zfin.gwt.ExpressionSmokeTest;
import org.zfin.gwt.MorpholinoAddSmokeTest;
import org.zfin.gwt.SimpleSmokeTest;
import org.zfin.gwt.lookup.LookupSmokeTest;
import org.zfin.gwt.marker.GeneEditSmokeTest;
import org.zfin.marker.webservice.MarkerWebServiceTest;
import org.zfin.uniquery.smoketest.SiteSearchSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AnatomySmokeTest.class,
        AntibodySmokeTest.class,
//        AntibodyEditSmokeTest.class // speed issues on embryonix make this unstable
        ExpressionSmokeTest.class,
        GeneEditSmokeTest.class,
        GenotypeDetailSmokeTest.class,
        LookupSmokeTest.class,
//        MarkerViewSmokeTest.class,
        MarkerWebServiceTest.class,
        MorpholinoAddSmokeTest.class,
        SimpleSmokeTest.class,
        SiteSearchSmokeTest.class
})
public class SmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SmokeTests.class);
    }

}
