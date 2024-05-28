package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.datatransfer.ncbi.NCBIReleaseFetcherTest;

/**
 * This is the unit test class that runs all registered unit tests (suite)
 * that require a network connection for downloads, etc..
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        NCBIReleaseFetcherTest.class
})

public class NetworkUnitTests {

}
