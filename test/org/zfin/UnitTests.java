package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.zfin.marker.presentation.MarkerPresentationTest;
import org.junit.runners.Suite;
import org.zfin.anatomy.presentation.*;
import org.zfin.orthology.OrthologyEvidenceFastSearchTest;
import org.zfin.orthology.OrthologyRepositoryTest;
import org.zfin.security.Md5PasswordEncoderTest;
import org.zfin.sequence.reno.presentation.RunCandidatePresentationTest;
import org.zfin.sequence.reno.presentation.RunPresentationTest;
import org.zfin.sequence.reno.presentation.SingleAssociatedGenesFromQueryTest;
import org.zfin.util.FileUtilTest;
import org.zfin.util.ListFormatterTest;
import org.zfin.util.UrlCreatorTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AnatomyPresentationTest.class,
        FileUtilTest.class,
        ListFormatterTest.class,
        MarkerPresentationTest.class,
        OrthologyEvidenceFastSearchTest.class,
        OrthologyValidationTest.class,
        OrthologyRepositoryTest.class,
        Md5PasswordEncoderTest.class,
        RelationshipTypeSortingTest.class,
        RunCandidatePresentationTest.class,
        SingleAssociatedGenesFromQueryTest.class,
        RunPresentationTest.class,
        StagePresentationTest.class,
        SortAnatomyResultsTest.class,
        UrlCreatorTest.class
        })

public class UnitTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(UnitTests.class);
    }
}
