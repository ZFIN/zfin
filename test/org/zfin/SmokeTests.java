package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.datatransfer.DownloadSmokeTest;
import org.zfin.expression.presentation.FigureSummarySmokeTest;
import org.zfin.feature.presentation.FeatureDetailSmokeTest;
import org.zfin.feature.presentation.GenotypeDetailSmokeTest;
import org.zfin.figure.presentation.FigureViewWebSpec;
import org.zfin.fish.smoketest.FishSmokeTest;
import org.zfin.fish.smoketest.PhenotypeSummarySmokeTest;
import org.zfin.gwt.lookup.LookupSmokeTest;
import org.zfin.httpunittest.MarkerViewSmokeTest;
import org.zfin.mapping.MappingDetailSmokeTest;
import org.zfin.marker.MarkerselectWebSpec;
import org.zfin.mutant.smoketest.ConstructSmokeTest;
import org.zfin.ontology.presentation.OntologyWebSpec;
import org.zfin.search.presentation.SearchWebSpec;
import org.zfin.sequence.blast.smoketest.BlastSmokeTest;
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
        FeatureDetailSmokeTest.class,
        FigureSummarySmokeTest.class,
        FishSmokeTest.class,
        PhenotypeSummarySmokeTest.class,
        FigureViewWebSpec.class,
        ConstructSmokeTest.class,
        GenotypeDetailSmokeTest.class,
        MarkerselectWebSpec.class,
        LookupSmokeTest.class,
        MappingDetailSmokeTest.class,
        MarkerSoapSmokeTest.class,
        MarkerSoapClientSmokeTest.class,
        MarkerViewSmokeTest.class,
        MarkerRestSmokeTest.class,
        OntologyWebSpec.class,
        SearchWebSpec.class
})
public class SmokeTests {

}
