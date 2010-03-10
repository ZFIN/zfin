package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.gwt.ExpressionJWebUnitTest;
import org.zfin.gwt.GeneralSecureJWebUnitTest;
import org.zfin.httpunittest.SmokeTest;

/**
 * Smoke tests: Integration tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SmokeTest.class,
        GeneralSecureJWebUnitTest.class,
        ExpressionJWebUnitTest.class
//        GOEvidenceUnitTest.class
})
public class SmokeTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(SmokeTests.class);
    }

}
