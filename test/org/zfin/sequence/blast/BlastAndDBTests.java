package org.zfin.sequence.blast;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is the master unit test class that runs all registered unit tests (suite)
 * that require a database connection.
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlastAccessTest.class ,
        //ExecuteBlastTest.class,
        //BlastDownloadsTest.class
        })

public class BlastAndDBTests {

}
