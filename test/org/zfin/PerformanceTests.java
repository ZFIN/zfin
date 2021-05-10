package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.marker.GenePerformanceTest;


/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        GenePerformanceTest.class
})

public class PerformanceTests {

}
