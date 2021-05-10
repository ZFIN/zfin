package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.framework.api.SequenceControllerTest;
import org.zfin.ontology.presentation.OntologyControllerTest;


/**
 * This is the master unit test class that runs all registered unit tests (suite)
 * that require a database connection.
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({

        OntologyControllerTest.class,
        SequenceControllerTest.class
})

public class DbControllerTests {

}
