package org.zfin.sequence.blast;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import org.zfin.TestConfiguration;

import junit.framework.JUnit4TestAdapter;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlastStressSmallDBTest.class
        , BlastStressLargeDBTest.class
        // , BlastStressLargeSequenceDBTest.class // this is a LONG test
})

/**
 * See fogbugz 3970.
 * For M accessions and P databases, need to get
 *
 */
public class BlastStressSuite {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(BlastStressSuite.class);
    }
}