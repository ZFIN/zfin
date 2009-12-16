package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.infrastructure.EnumValidationTest;

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
