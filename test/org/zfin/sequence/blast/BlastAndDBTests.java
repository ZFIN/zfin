package org.zfin.sequence.blast;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.TestConfiguration;

/**
 * This is the master unit test class that runs all registered unit tests (suite)
 * that require a database connection.
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlastAccessTest.class ,
        ExecuteBlastTest.class,
        BlastDownloadsTest.class
        })

public class BlastAndDBTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(BlastAndDBTests.class);
    }
}
