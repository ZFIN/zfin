package org.zfin.sequence.blast;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.TestConfiguration;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        BlastHeuristicsEvaluator.class
})

/**
 * See fogbugz 3970.
 * For M accessions and P databases, need to get
 *
 */
public class BlastHeuristicsEvaluatorSuite {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(BlastHeuristicsEvaluatorSuite.class);
    }
}