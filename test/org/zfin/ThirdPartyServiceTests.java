package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.datatransfer.doi.DOITest;
import org.zfin.datatransfer.microarray.MicroarrayServiceTest;
import org.zfin.sequence.blast.BlastDBServiceTest;
import org.zfin.sequence.blast.WebServiceSoapClientTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        MicroarrayServiceTest.class,
        WebServiceSoapClientTest.class,
        BlastDBServiceTest.class,
        DOITest.class
//        PostForeignTest.class
})

public class ThirdPartyServiceTests {

}