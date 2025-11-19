package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.gwt.curation.PileConstructionSmokeTest;
import org.zfin.gwt.marker.AntibodyEditSmokeTest;

/**
 * Smoke tests that make changes to the database: login requires a person setup and other edit pages
 * will make changes to the data.
 * Note: Do not run on production.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

        AntibodyEditSmokeTest.class,
        PileConstructionSmokeTest.class

})
public class SmokeTestsReadWrite {


}
