package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.datatransfer.doi.DOITest;
import org.zfin.datatransfer.microarray.MicroarrayServiceTest;
import org.zfin.sequence.blast.BlastServiceTest;
import org.zfin.wiki.WikiConnectTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        WikiConnectTest.class,
        MicroarrayServiceTest.class,
        BlastServiceTest.class,
        DOITest.class
//        PostForeignTest.class
        })

public class ThirdPartyServiceTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(ThirdPartyServiceTests.class);
    }
}