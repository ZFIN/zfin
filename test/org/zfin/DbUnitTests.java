package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.repository.AnatomyRepositoryTest;
import org.zfin.antibody.AntibodyServiceTest;
import org.zfin.antibody.repository.AntibodyRepositoryTest;
import org.zfin.curation.repository.CurationRepositoryTest;
import org.zfin.curation.service.CurationDTOConversionServiceSpec;
import org.zfin.database.InformixUtil;
import org.zfin.database.presentation.DatabaseServiceDbTest;
import org.zfin.datatransfer.go.GafParserUnitTest;
import org.zfin.datatransfer.go.service.FpInferenceGafServiceTest;
import org.zfin.datatransfer.go.service.GafServiceTest;
import org.zfin.datatransfer.go.service.GoaGafServiceTest;
import org.zfin.datatransfer.go.service.PaintGafServiceTest;
import org.zfin.expression.FigureServiceTest;
import org.zfin.expression.repository.ExpressionRepositoryTest;
import org.zfin.expression.service.ExpressionServiceTest;
import org.zfin.feature.FeatureServiceTest;
import org.zfin.feature.repository.FeatureRepositoryTest;
import org.zfin.figure.repository.FigureRepositorySpec;
import org.zfin.figure.service.FigureViewServiceSpec;
import org.zfin.figure.service.ImageServiceSpec;
import org.zfin.figure.service.VideoServiceSpec;
import org.zfin.fish.repository.FishRepositoryTest;
import org.zfin.fish.repository.FishServiceTest;
import org.zfin.framework.presentation.ZfinJSPFunctionsTest;
import org.zfin.gbrowse.GBrowseServiceTest;
import org.zfin.gbrowse.presentation.GBrowseImageSpec;
import org.zfin.gwt.GwtConversionTest;
import org.zfin.gwt.curation.CurationRPCTest;
import org.zfin.gwt.marker.GoEvidenceTest;
import org.zfin.gwt.root.server.DTOConversionServiceTest;
import org.zfin.infrastructure.InfrastructureRepositoryTest;
import org.zfin.infrastructure.delete.DeleteRuleTest;
import org.zfin.mapping.repository.LinkageRepositoryTest;
import org.zfin.marker.MarkerServiceTest;
import org.zfin.marker.MergeMarkerDBTest;
import org.zfin.marker.presentation.GeneAddFormBeanValidatorSpec;
import org.zfin.marker.presentation.MarkerGoServiceIntegrationSpec;
import org.zfin.marker.presentation.SequenceTargetingReagentAddBeanValidatorSpec;
import org.zfin.marker.repository.MarkerRepositoryTest;
import org.zfin.marker.repository.TranscriptRepositoryTest;
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
import org.zfin.publication.PublicationServiceSpec;
import org.zfin.publication.presentation.PublicationValidatorSpec;
import org.zfin.publication.repository.PublicationRepositoryTest;
import org.zfin.sequence.DisplayGroupRepositoryTest;
import org.zfin.sequence.MapAccessionDbLinkTest;
import org.zfin.sequence.SequenceRepositoryTest;
import org.zfin.sequence.TranscriptServiceTest;
import org.zfin.sequence.blast.BlastRepositoryTest;
import org.zfin.sequence.reno.AlignmentsControllerTest;
import org.zfin.sequence.reno.OrthologyTest;
import org.zfin.sequence.reno.RenoMultiRunTest;
import org.zfin.sequence.reno.RenoRepositoryTest;
import org.zfin.sequence.reno.presentation.RenoRedundancyCandidateControllerTest;
import org.zfin.sequence.reno.presentation.SingleAssociatedGenesFromQueryTest;
import org.zfin.sequence.reno.repository.SingleCandidateRepositoryTest;
import org.zfin.uniquery.SequenceIdListTest;
import org.zfin.webservice.MarkerSoapDbTest;
import org.zfin.wiki.service.AntibodyWikiWebServiceTest;

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
        AntibodyWikiWebServiceTest.class,
        BlastRepositoryTest.class,
        CurationDTOConversionServiceSpec.class,
        CurationRepositoryTest.class,
        CurationRPCTest.class,
        DatabaseServiceDbTest.class,
        DeleteRuleTest.class,
        DisplayGroupRepositoryTest.class,
        DTOConversionServiceTest.class,
        ExpressionRepositoryTest.class,
        ExpressionServiceTest.class,
        FeatureRepositoryTest.class,
        FeatureServiceTest.class,
        FigureRepositorySpec.class,
        FigureServiceTest.class,
        FigureViewServiceSpec.class,
        FishRepositoryTest.class,
        FishServiceTest.class,
        FpInferenceGafServiceTest.class,
        GafParserUnitTest.class,
        GafServiceTest.class,
        GBrowseImageSpec.class,
        GBrowseServiceTest.class,
        GeneAddFormBeanValidatorSpec.class,
        GoaGafServiceTest.class,
        GoEvidenceTest.class,
        GwtConversionTest.class,
        ImageServiceSpec.class,
        InformixUtil.class,
        InfrastructureRepositoryTest.class,
        LinkageRepositoryTest.class,
        MapAccessionDbLinkTest.class,
        MarkerGoServiceIntegrationSpec.class,
        MarkerGoTermEvidenceRepositoryTest.class,
        MarkerRepositoryTest.class,
        MarkerServiceTest.class,
        MarkerSoapDbTest.class,
        MergeMarkerDBTest.class,
//        MicroArrayTest.class,     // Takes 1.5 min to run, but works.
        MutantRepositoryTest.class,
//        NomenclatureCandidateControllerTest.class, // no tests
        OntologyManagerTest.class,
        OntologyRepositoryTest.class,
        OntologySerializationTest.class,
        OrthologyRepositoryTest.class,
        OrthologyTest.class,
        PaginationResultTest.class,
        PaintGafServiceTest.class,
        PhenotypeRepositoryTest.class,
        PhenotypeServiceDBTest.class,
        ProfileRepositoryTest.class,
        ProfileServiceTest.class,
        PublicationRepositoryTest.class,
        PublicationServiceSpec.class,
        PublicationValidatorSpec.class,
        RenoMultiRunTest.class,
        RenoRedundancyCandidateControllerTest.class,
        RenoRepositoryTest.class,
        SequenceIdListTest.class,
        //SequenceRepositorySpec.class,
        SequenceRepositoryTest.class,
        SequenceTargetingReagentAddBeanValidatorSpec.class,
        SingleAssociatedGenesFromQueryTest.class,
        SingleCandidateRepositoryTest.class,
        TranscriptRepositoryTest.class,
        TranscriptServiceTest.class,
        VideoServiceSpec.class,
        ZfinJSPFunctionsTest.class,
})

public class DbUnitTests {

}
