package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.datatransfer.DownloadSmokeTest;
import org.zfin.expression.FigureViewSmokeTest;
import org.zfin.expression.presentation.FigureSummarySmokeTest;
import org.zfin.feature.presentation.GenotypeDetailSmokeTest;
import org.zfin.fish.smoketest.FishSmokeTest;
import org.zfin.gwt.ExpressionSmokeTest;
import org.zfin.httpunittest.MarkerViewSmokeTest;
import org.zfin.mutant.smoketest.ConstructSmokeTest;
import org.zfin.ontology.presentation.OntologySmokeTest;
import org.zfin.publication.presentation.PublicationCloseSmokeTest;
import org.zfin.sequence.blast.smoketest.BlastSmokeTest;
import org.zfin.uniquery.smoketest.SiteSearchSmokeTest;
import org.zfin.webservice.MarkerRestSmokeTest;
import org.zfin.webservice.MarkerSoapClientSmokeTest;
import org.zfin.webservice.MarkerSoapSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

        AnatomySmokeTest.class,
        AntibodySmokeTest.class,
        BlastSmokeTest.class,
        DownloadSmokeTest.class,
// commenting out as those are flaky in running on linux.
//        ExpressionSmokeTest.class,
        FigureSummarySmokeTest.class,
        FishSmokeTest.class,
        FigureViewSmokeTest.class,
        ConstructSmokeTest.class,
        GenotypeDetailSmokeTest.class,
        MarkerSoapSmokeTest.class,
        MarkerSoapClientSmokeTest.class,
        MarkerViewSmokeTest.class,
        MarkerRestSmokeTest.class,
        OntologySmokeTest.class,
        PublicationCloseSmokeTest.class,
        SiteSearchSmokeTest.class

})
public class SmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SmokeTests.class);
    }

}
