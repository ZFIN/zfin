package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.DevelopmentStageTest;
import org.zfin.anatomy.repository.AnatomyRepositoryTest;
import org.zfin.antibody.AntibodyServiceTest;
import org.zfin.antibody.repository.AntibodyRepositoryTest;
import org.zfin.construct.ConstructTest;
import org.zfin.curation.repository.CurationRepositoryTest;
import org.zfin.database.InformixUtilTest;
import org.zfin.database.presentation.DatabaseServiceDbTest;
import org.zfin.datatransfer.go.GafParserUnitTest;
import org.zfin.datatransfer.go.service.FpInferenceGafServiceTest;
import org.zfin.datatransfer.go.service.GafServiceTest;
import org.zfin.datatransfer.go.service.GoaGafServiceTest;
import org.zfin.expression.FigureServiceTest;
import org.zfin.expression.repository.ExpressionRepositoryTest;
import org.zfin.expression.service.ExpressionServiceTest;
import org.zfin.feature.FeatureServiceTest;
import org.zfin.feature.FeatureTrackingTest;
import org.zfin.feature.repository.FeatureRepositoryTest;
import org.zfin.feature.service.FeatureAttributionServiceTest;
import org.zfin.fish.repository.FishRepositoryTest;
import org.zfin.fish.repository.FishServiceTest;
import org.zfin.gbrowse.GBrowseServiceTest;
import org.zfin.gwt.curation.CurationRPCTest;
import org.zfin.gwt.marker.GoEvidenceTest;
import org.zfin.gwt.root.server.DTOConversionServiceTest;
import org.zfin.infrastructure.InfrastructureRepositoryTest;
import org.zfin.infrastructure.RecordAttributionTest;
import org.zfin.infrastructure.delete.DeleteRuleTest;
import org.zfin.mapping.MappingServiceTest;
import org.zfin.mapping.repository.LinkageRepositoryTest;
import org.zfin.marker.MarkerAttributionServiceTest;
import org.zfin.marker.MarkerChromosomalLocationTest;
import org.zfin.marker.MarkerServiceTest;
import org.zfin.marker.MergeMarkerDBTest;
import org.zfin.marker.repository.MarkerRepositoryTest;
import org.zfin.marker.repository.TranscriptRepositoryTest;
import org.zfin.mutant.MutantEntityMappingTest;
import org.zfin.mutant.PhenotypeServiceDBTest;
import org.zfin.mutant.repository.MarkerGoTermEvidenceRepositoryTest;
import org.zfin.mutant.repository.MutantRepositoryTest;
import org.zfin.mutant.repository.PhenotypeRepositoryTest;
import org.zfin.ontology.OntologyManagerTest;
import org.zfin.ontology.OntologySerializationTest;
import org.zfin.ontology.repository.OntologyRepositoryTest;
import org.zfin.orthology.OrthologyRepositoryTest;
import org.zfin.profile.repository.ProfileRepositoryTest;
import org.zfin.profile.service.ProfileServiceTest;
import org.zfin.publication.repository.PublicationRepositoryTest;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.BlastRepositoryTest;
import org.zfin.sequence.reno.AlignmentsControllerTest;
import org.zfin.sequence.reno.OrthologyTest;
import org.zfin.sequence.reno.RenoMultiRunTest;
import org.zfin.sequence.reno.RenoRepositoryTest;
import org.zfin.sequence.reno.presentation.RenoRedundancyCandidateControllerTest;
import org.zfin.sequence.reno.presentation.SingleAssociatedGenesFromQueryTest;
import org.zfin.sequence.reno.repository.SingleCandidateRepositoryTest;

//import org.zfin.fish.repository.FishServiceTest;


/**
 * This is the master unit test class that runs all registered unit tests (suite)
 * that require a database connection.
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    AlignmentsControllerTest.class,
    AnatomyRepositoryTest.class,
    AntibodyRepositoryTest.class,
    AntibodyServiceTest.class,
    BlastRepositoryTest.class,
    ConstructTest.class,
    CurationRepositoryTest.class,
    CurationRPCTest.class,
    DatabaseServiceDbTest.class,
    DeleteRuleTest.class,
    DevelopmentStageTest.class,
    DisplayGroupRepositoryTest.class,
    DTOConversionServiceTest.class,
    ExpressionRepositoryTest.class,
    ExpressionServiceTest.class,
    FeatureAttributionServiceTest.class,
    FeatureRepositoryTest.class,
    FeatureServiceTest.class,
    FeatureTrackingTest.class,
    FigureServiceTest.class,
    FishRepositoryTest.class,
    FishServiceTest.class,
    FpInferenceGafServiceTest.class,
    GafParserUnitTest.class,
    GafServiceTest.class,
    GBrowseServiceTest.class,
    GoaGafServiceTest.class,
    GoEvidenceTest.class,
    InformixUtilTest.class,
    InfrastructureRepositoryTest.class,
    LinkageRepositoryTest.class,
    MapAccessionDbLinkTest.class,
    MappingServiceTest.class,
    MarkerChromosomalLocationTest.class,
    MarkerGoTermEvidenceRepositoryTest.class,
    MarkerRepositoryTest.class,
    MarkerServiceTest.class,
    MarkerAttributionServiceTest.class,
    MergeMarkerDBTest.class,
//        MicroArrayTest.class,     // Takes 1.5 min to run, but works.
    MutantEntityMappingTest.class,
    MutantRepositoryTest.class,
//        NomenclatureCandidateControllerTest.class, // no tests
    OntologyManagerTest.class,
    OntologyRepositoryTest.class,
    OntologySerializationTest.class,
    OrthologyRepositoryTest.class,
    OrthologyTest.class,
    PaginationResultTest.class,
    PhenotypeRepositoryTest.class,
    PhenotypeServiceDBTest.class,
    ProfileRepositoryTest.class,
    ProfileServiceTest.class,
    PublicationRepositoryTest.class,
//        PublicationRepositoryRefactorTest.class, //temporarily used while refactoring PublicationRepository
    RecordAttributionTest.class,
    RenoMultiRunTest.class,
    RenoRedundancyCandidateControllerTest.class,
    RenoRepositoryTest.class,
    SequenceRepositoryTest.class,
    SequenceServiceTest.class,
    SingleAssociatedGenesFromQueryTest.class,
    SingleCandidateRepositoryTest.class,
    TranscriptRepositoryTest.class,
    TranscriptServiceTest.class,
})

public class DbUnitTests {

}
