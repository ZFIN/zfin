package org.zfin;

import de.oschoen.junit.runner.BatchTestRunner;
import org.jenkinsci.testinprogress.runner.ProgressSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.AnatomyItemTest;
import org.zfin.anatomy.presentation.AnatomyPresentationTest;
import org.zfin.anatomy.presentation.RelationshipTypeSortingTest;
import org.zfin.anatomy.presentation.SortAnatomyResultsTest;
import org.zfin.anatomy.presentation.StagePresentationTest;
import org.zfin.antibody.presentation.AntibodySearchCriteriaTest;
import org.zfin.database.DatabaseServiceTest;
import org.zfin.database.presentation.TableTest;
import org.zfin.datatransfer.microarray.MicroarrayParseTest;
import org.zfin.expression.CurationExperimentTest;
import org.zfin.expression.ExpressionExperimentTest;
import org.zfin.feature.service.MutationDetailsConversionServiceSpec;
import org.zfin.framework.ExecProcessTest;
import org.zfin.framework.mail.MailTest;
import org.zfin.framework.presentation.*;
import org.zfin.framework.search.SearchCriterionTest;
import org.zfin.gwt.GwtStringUtilsTest;
import org.zfin.gwt.InferenceUnitTest;
import org.zfin.gwt.curation.ui.FeatureMarkerRelationshipTest;
import org.zfin.gwt.curation.ui.FeatureValidationTest;
import org.zfin.gwt.curation.ui.PatoPileStructureValidatorTest;
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
import org.zfin.infrastructure.ant.ReportConfigurationTest;
import org.zfin.marker.MergeMarkerUnitTest;
import org.zfin.marker.presentation.DbLinkDisplayComparatorTest;
import org.zfin.marker.presentation.MarkerPresentationTest;
import org.zfin.mutant.PhenotypeServiceTest;
import org.zfin.mutant.PhenotypeStatementTest;
import org.zfin.mutant.PhenotypeStructureTest;
import org.zfin.ontology.GenericTermTest;
import org.zfin.ontology.MatchingTermServiceTest;
import org.zfin.ontology.OntologyTokenizationTest;
import org.zfin.ontology.presentation.DiseaseDisplayComparatorTest;
import org.zfin.ontology.presentation.ExpressionResultPresentationTest;
import org.zfin.ontology.service.OntologyServiceTest;
import org.zfin.profile.OrganizationUrlTest;
import org.zfin.profile.ProfileUnitTests;
import org.zfin.profile.service.BeanCompareServiceTest;
import org.zfin.properties.ZfinPropertiesTest;
import org.zfin.publication.MeshHeadingSpec;
import org.zfin.publication.PubMedValidationReportTest;
import org.zfin.publication.PublicationTest;
import org.zfin.security.Md5PasswordEncoderTest;
import org.zfin.sequence.blast.BlastNonDBTest;
import org.zfin.sequence.blast.SequenceTest;
import org.zfin.sequence.blast.presentation.BlastPresentationServiceTest;
import org.zfin.sequence.reno.presentation.RunCandidatePresentationTest;
import org.zfin.sequence.reno.presentation.RunPresentationTest;
import org.zfin.uniquery.IndexerUtilTest;
import org.zfin.uniquery.SiteSearchTest;
import org.zfin.util.*;


/**
 * This is the master unit test class that runs all registered unit tests (suite).
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(ProgressSuite.class)
@BatchTestRunner.BatchTestInclude("**.*Suite")
@Suite.SuiteClasses({
        ActiveDataTest.class,
        ActiveSourceTest.class,
        AnatomyItemTest.class,
        AnatomyPresentationTest.class,
        AntibodySearchCriteriaTest.class,
        ApgPaginationBeanTest.class,
        BeanCompareServiceTest.class,
        BlastNonDBTest.class,
        BlastPresentationServiceTest.class,
//        BtsContainsServiceTest.class,
        CurationExperimentTest.class,
        DatabaseServiceTest.class,
        DbLinkDisplayComparatorTest.class,
        DbScriptFileParserTest.class,
        DiseaseDisplayComparatorTest.class,
        EntityPresentationTest.class,
        EnvironmentDTOTest.class,
        ExperimentDTOTest.class,
        ExpressionExperimentTest.class,
        ExpressionResultPresentationTest.class,
        FeatureMarkerRelationshipTest.class,
        FeatureValidationTest.class,
        FileUtilTest.class,
        FunctionsTest.class,
        GenericTermTest.class,
        GwtStringUtilsTest.class,
        HighlighterTest.class,
        HighlightUtilTest.class,
        IndexerUtilTest.class,
        InferenceUnitTest.class,
        ListFormatterTest.class,
        MailTest.class,
        MarkerPresentationTest.class,
        MatchingServiceTest.class,
        MatchingTermServiceTest.class,
        MatchTypeTest.class,
        Md5PasswordEncoderTest.class,
        MergeMarkerUnitTest.class,
        MeshHeadingSpec.class,
        MicroarrayParseTest.class,
        MutationDetailsConversionServiceSpec.class,
        OntologyServiceTest.class,
        OntologyTokenizationTest.class,
        OrganizationUrlTest.class,
        PaginationBeanTest.class,
        PatoPileStructureValidatorTest.class,
        PhenotypeServiceTest.class,
        PhenotypeStatementTest.class,
        PhenotypeStatementTest.class,
        PhenotypeStructureTest.class,
        ProfileUnitTests.class,
        PublicationTest.class,
        PubMedValidationReportTest.class,
        RelationshipTypeSortingTest.class,
        ReportConfigurationTest.class,
        ReportGeneratorSpec.class,
        RunCandidatePresentationTest.class,
        RunPresentationTest.class,
        SearchCriterionTest.class,
        SectionVisibilityTest.class,
        SequenceTest.class,
        SerializationTests.class,
        SiteSearchTest.class,
        SortAnatomyResultsTest.class,
        StagePresentationTest.class,
        StageRangeIntersectionTest.class,
        StageRangeUnionTest.class,
        TableTest.class,
        TermStageSplitStatementTest.class,
        TermStageUpdateFileParserTest.class,
        TrieMapTest.class,
        UIFieldTransformerTest.class,
        UrlCreatorTest.class,
        WidgetUtilTest.class,
        ZfinPropertiesTest.class,
        ZfinStringUtilsTest.class,

        // this test should be last (or near last)
        // because we need to make sure that it waits
        // for thread generation to finish
        ExecProcessTest.class
})

public class UnitTests {

    @BeforeClass
    public static void setUpClass() {
        TestConfiguration.configure();
    }
}
