package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomyItemTest;
import org.zfin.anatomy.presentation.*;
import org.zfin.antibody.presentation.AntibodySearchCriteriaTest;
import org.zfin.datatransfer.microarray.MicroarrayParseTest;
import org.zfin.expression.CurationExperimentTest;
import org.zfin.expression.ExpressionExperimentTest;
import org.zfin.framework.ExecProcessTest;
import org.zfin.framework.mail.MailTest;
import org.zfin.framework.presentation.*;
import org.zfin.gwt.GwtStringUtilsTest;
import org.zfin.gwt.InferenceUnitTest;
import org.zfin.gwt.curation.ui.FeatureMarkerRelationshipTest;
import org.zfin.gwt.curation.ui.FeatureValidationTest;
import org.zfin.gwt.root.dto.EnvironmentDTOTest;
import org.zfin.gwt.root.dto.ExperimentDTOTest;
import org.zfin.gwt.root.server.HighlighterTest;
import org.zfin.gwt.root.util.StageRangeIntersectionTest;
import org.zfin.gwt.root.util.StageRangeUnionTest;
import org.zfin.gwt.root.util.WidgetUtilTest;
import org.zfin.infrastructure.ActiveDataTest;
import org.zfin.infrastructure.ActiveSourceTest;
import org.zfin.infrastructure.SerializationTests;
import org.zfin.infrastructure.TrieMapTest;
import org.zfin.marker.MarkerServiceTest;
import org.zfin.marker.MergeMarkerUnitTest;
import org.zfin.marker.presentation.MarkerPresentationTest;
import org.zfin.mutant.PhenotypeServiceTest;
import org.zfin.mutant.PhenotypeStructureTest;
import org.zfin.mutant.presentation.MorpholinoStatisticsTest;
import org.zfin.ontology.GenericTermTest;
import org.zfin.ontology.MatchingTermServiceTest;
import org.zfin.ontology.OntologyTokenizationTest;
import org.zfin.datatransfer.go.GafParserUnitTest;
import org.zfin.ontology.presentation.ExpressionResultPresentationTest;
import org.zfin.orthology.OrthologyEvidenceFastSearchTest;
import org.zfin.people.OrganizationUrlTest;
import org.zfin.properties.ZfinPropertiesTest;
import org.zfin.publication.PublicationTest;
import org.zfin.security.Md5PasswordEncoderTest;
import org.zfin.sequence.blast.BlastNonDBTest;
import org.zfin.sequence.blast.presentation.BlastPresentationServiceTest;
import org.zfin.sequence.reno.presentation.RunCandidatePresentationTest;
import org.zfin.sequence.reno.presentation.RunPresentationTest;
import org.zfin.sequence.reno.presentation.SingleAssociatedGenesFromQueryTest;
import org.zfin.uniquery.SiteSearchTest;
import org.zfin.util.*;


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
        BlastNonDBTest.class,
        BlastPresentationServiceTest.class,
        CurationExperimentTest.class,
        EntityPresentationTest.class,
        EnvironmentDTOTest.class,
        ExpressionExperimentTest.class,
        ExpressionResultPresentationTest.class,
        ExperimentDTOTest.class,
        FeatureMarkerRelationshipTest.class,
        FeatureValidationTest.class,
        FileUtilTest.class,
        FunctionsTest.class,
        InferenceUnitTest.class,
        GafParserUnitTest.class,
        GenericTermTest.class,
        GwtStringUtilsTest.class,
        HighlightUtilTest.class,
        HighlighterTest.class,
        ListFormatterTest.class,
        MailTest.class,
        MarkerPresentationTest.class,
        MarkerServiceTest.class,
        Md5PasswordEncoderTest.class,
        MicroarrayParseTest.class,
        MergeMarkerUnitTest.class,
        MorpholinoStatisticsTest.class,
        OntologyTokenizationTest.class,
        OrganizationUrlTest.class,
        OrthologyEvidenceFastSearchTest.class,
        OrthologyValidationTest.class,
        PaginationBeanTest.class,
        PhenotypeServiceTest.class,
        PhenotypeStructureTest.class,
        PublicationTest.class,
        RelationshipTypeSortingTest.class,
        RunCandidatePresentationTest.class,
        RunPresentationTest.class,
        SectionVisibilityTest.class,
        SerializationTests.class,
        SequenceTest.class,
        SingleAssociatedGenesFromQueryTest.class,
        SiteSearchTest.class,
        SortAnatomyResultsTest.class,
        StagePresentationTest.class,
        StageRangeIntersectionTest.class,
        StageRangeUnionTest.class,
        ZfinStringUtilsTest.class,
        TrieMapTest.class,
        UIFieldTransformerTest.class,
        UrlCreatorTest.class,
        WidgetUtilTest.class,
        ZfinPropertiesTest.class,

        ExecProcessTest.class
        // this test should be last (or near last)
        // because we need to make sure that it waits
        // for thread generation to finish
})

public class UnitTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(UnitTests.class);
    }
}
