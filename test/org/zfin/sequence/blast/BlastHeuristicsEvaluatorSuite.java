package org.zfin.sequence.blast;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.runners.Suite;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.hibernate.SessionFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.sequence.blast.presentation.XMLBlastBean;
import org.zfin.repository.RepositoryFactory;
import org.apache.log4j.Logger;
import org.apache.commons.math.stat.regression.SimpleRegression;

import java.util.*;
import junit.framework.JUnit4TestAdapter;


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