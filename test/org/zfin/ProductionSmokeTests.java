package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomySmokeTest;
import org.zfin.antibody.smoketest.AntibodySmokeTest;
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
        MarkerSoapSmokeTest.class,
        MarkerSoapClientSmokeTest.class,
        MarkerRestSmokeTest.class
})
public class ProductionSmokeTests {

}
