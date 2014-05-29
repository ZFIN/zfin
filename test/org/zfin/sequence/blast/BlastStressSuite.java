package org.zfin.sequence.blast;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


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

}