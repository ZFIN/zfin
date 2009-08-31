package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomyItemTest;
import org.zfin.anatomy.presentation.*;
import org.zfin.antibody.AntibodyServiceTest;
import org.zfin.antibody.presentation.AntibodySearchCriteriaTest;
import org.zfin.framework.presentation.FunctionsTest;
import org.zfin.framework.presentation.SectionVisibilityTest;
import org.zfin.framework.presentation.UIFieldTransformerTest;
import org.zfin.framework.presentation.PaginationBeanTest;
import org.zfin.infrastructure.ActiveDataTest;
import org.zfin.infrastructure.ActiveSourceTest;
import org.zfin.marker.presentation.MarkerPresentationTest;
import org.zfin.mutant.PhenotypeServiceTest;
import org.zfin.mutant.presentation.MorpholinoStatisticsTest;
import org.zfin.orthology.OrthologyEvidenceFastSearchTest;
import org.zfin.orthology.OrthologyRepositoryTest;
import org.zfin.people.OrganizationUrlTest;
import org.zfin.properties.ZfinPropertiesTest;
import org.zfin.security.Md5PasswordEncoderTest;
import org.zfin.sequence.reno.presentation.RunCandidatePresentationTest;
import org.zfin.sequence.reno.presentation.RunPresentationTest;
import org.zfin.sequence.reno.presentation.SingleAssociatedGenesFromQueryTest;
import org.zfin.util.FileUtilTest;
import org.zfin.util.HighlightUtilTest;
import org.zfin.util.ListFormatterTest;
import org.zfin.util.UrlCreatorTest;
import org.zfin.expression.ExpressionExperimentTest;
import org.zfin.expression.CurationExperimentTest;
import org.zfin.curation.StageRangeIntersectionTest;
import org.zfin.curation.WidgetUtilTest;
import org.zfin.curation.ExperimentDTOTest;
import org.zfin.curation.StageRangeUnionTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ActiveSourceTest.class,
        ActiveDataTest.class,
        AnatomyItemTest.class,
        AnatomyPresentationTest.class,
        AntibodySearchCriteriaTest.class,
        AntibodyServiceTest.class,
        CurationExperimentTest.class,
        ExpressionExperimentTest.class,
        ExperimentDTOTest.class,
        FileUtilTest.class,
        FunctionsTest.class,
        HighlightUtilTest.class,
        ListFormatterTest.class,
        MarkerPresentationTest.class,
        Md5PasswordEncoderTest.class,
        MorpholinoStatisticsTest.class,
        OrganizationUrlTest.class,
        OrthologyEvidenceFastSearchTest.class,
        OrthologyValidationTest.class,
        OrthologyRepositoryTest.class,
        PaginationBeanTest.class,
        PhenotypeServiceTest.class,
        RelationshipTypeSortingTest.class,
        RunCandidatePresentationTest.class,
        RunPresentationTest.class,
        SectionVisibilityTest.class,
        SingleAssociatedGenesFromQueryTest.class,
        StageRangeIntersectionTest.class,
        StageRangeUnionTest.class,
        StagePresentationTest.class,
        SortAnatomyResultsTest.class,
        UIFieldTransformerTest.class,
        UrlCreatorTest.class,
        WidgetUtilTest.class,
        ZfinPropertiesTest.class
})

public class UnitTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(UnitTests.class);
    }
}
