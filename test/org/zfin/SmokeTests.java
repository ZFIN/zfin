package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.expression.presentation.FigureSummarySmokeTest;
import org.zfin.feature.presentation.GenotypeDetailSmokeTest;
import org.zfin.gwt.ExpressionSmokeTest;
import org.zfin.gwt.MorpholinoAddSmokeTest;
import org.zfin.gwt.SimpleSmokeTest;
import org.zfin.gwt.lookup.LookupSmokeTest;
import org.zfin.gwt.marker.GeneEditSmokeTest;
import org.zfin.ontology.presentation.OntologySmokeTest;
import org.zfin.uniquery.smoketest.SiteSearchSmokeTest;
import org.zfin.webservice.MarkerRestSmokeTest;
import org.zfin.webservice.MarkerSoapClientSmokeTest;
import org.zfin.webservice.MarkerSoapSmokeTest;
import org.zfin.gwt.curation.PileConstructionSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AnatomySmokeTest.class,
        AntibodySmokeTest.class,
//        AntibodyEditSmokeTest.class // speed issues on embryonix make this unstable
        ExpressionSmokeTest.class,
        FigureSummarySmokeTest.class,
        GeneEditSmokeTest.class,
        GenotypeDetailSmokeTest.class,
        LookupSmokeTest.class,
        MarkerSoapSmokeTest.class,
        MarkerSoapClientSmokeTest.class,
//        MarkerViewSmokeTest.class,
        MarkerRestSmokeTest.class,
        MorpholinoAddSmokeTest.class,
        OntologySmokeTest.class,
        PileConstructionSmokeTest.class,
        SimpleSmokeTest.class,
        SiteSearchSmokeTest.class

})
public class SmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SmokeTests.class);
    }

}
