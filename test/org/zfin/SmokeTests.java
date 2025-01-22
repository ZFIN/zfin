package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
import org.zfin.datatransfer.DownloadSmokeTest;
import org.zfin.expression.presentation.FigureSummarySmokeTest;
import org.zfin.feature.presentation.FeatureDetailSmokeTest;
import org.zfin.feature.presentation.GenotypeDetailSmokeTest;
import org.zfin.fish.smoketest.FishSmokeTest;
import org.zfin.fish.smoketest.PhenotypeSummarySmokeTest;
import org.zfin.gwt.lookup.LookupSmokeTest;
import org.zfin.httpunittest.MarkerViewSmokeTest;
import org.zfin.mapping.MappingDetailSmokeTest;
import org.zfin.marker.MarkerStrSmokeTest;
import org.zfin.mutant.smoketest.ConstructSmokeTest;
import org.zfin.sequence.blast.smoketest.BlastSmokeTest;
import org.zfin.webservice.MarkerRestSmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

    AnatomySmokeTest.class,
    AntibodySmokeTest.class,
    BlastSmokeTest.class,
    DownloadSmokeTest.class,
    FeatureDetailSmokeTest.class,
    FigureSummarySmokeTest.class,
    FishSmokeTest.class,
    PhenotypeSummarySmokeTest.class,
    ConstructSmokeTest.class,
    GenotypeDetailSmokeTest.class,
    LookupSmokeTest.class,
    MappingDetailSmokeTest.class,
    MarkerStrSmokeTest.class,
    MarkerViewSmokeTest.class,
    MarkerRestSmokeTest.class,

//TODO: fix these webspecs and uncomment them
//        FigureViewWebSpec.class,
//        MarkerselectWebSpec.class,
//        OntologyWebSpec.class,
//        SearchWebSpec.class
})
public class SmokeTests {

}
