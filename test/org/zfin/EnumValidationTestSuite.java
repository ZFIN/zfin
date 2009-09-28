package org.zfin;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.zfin.TestConfiguration;
import org.zfin.infrastructure.EnumValidationTest;
import junit.framework.JUnit4TestAdapter;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        EnumValidationTest.class
})
public class EnumValidationTestSuite {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(EnumValidationTestSuite.class);
    }
}
