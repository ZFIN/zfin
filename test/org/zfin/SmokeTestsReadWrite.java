package org.zfin;

import org.jenkinsci.testinprogress.runner.ProgressSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.gwt.curation.PileConstructionSmokeTest;
import org.zfin.gwt.marker.AntibodyEditSmokeTest;
import org.zfin.gwt.marker.GeneEditSmokeTest;

/**
 * Smoke tests that make changes to the database: login requires a person setup and other edit pages
 * will make changes to the data.
 * Note: Do not run on production.
 */
@RunWith(ProgressSuite.class)
@Suite.SuiteClasses({

        AntibodyEditSmokeTest.class,
        GeneEditSmokeTest.class,
        PileConstructionSmokeTest.class

})
public class SmokeTestsReadWrite {


}
