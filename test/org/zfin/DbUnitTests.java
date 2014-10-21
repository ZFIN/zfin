package org.zfin;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.repository.AnatomyRepositoryTest;
import org.zfin.antibody.AntibodyServiceTest;
import org.zfin.antibody.repository.AntibodyRepositoryTest;
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
import org.zfin.fish.repository.FishRepositoryTest;
import org.zfin.fish.repository.FishServiceTest;
import org.zfin.framework.presentation.ZfinJSPFunctionsTest;
import org.zfin.gbrowse.GBrowseServiceTest;
import org.zfin.gwt.GwtConversionTest;
import org.zfin.gwt.curation.CurationRPCTest;
import org.zfin.gwt.marker.GoEvidenceTest;
import org.zfin.gwt.root.server.DTOConversionServiceTest;
import org.zfin.infrastructure.InfrastructureRepositoryTest;
import org.zfin.mapping.repository.LinkageRepositoryTest;
import org.zfin.marker.MarkerServiceTest;
import org.zfin.marker.MergeMarkerDBTest;
import org.zfin.marker.repository.MarkerRepositoryTest;
import org.zfin.marker.repository.TranscriptRepositoryTest;
import org.zfin.mutant.PhenotypeServiceDBTest;
import org.zfin.mutant.repository.*;
import org.zfin.ontology.OntologyManagerTest;
import org.zfin.ontology.OntologySerializationTest;
import org.zfin.ontology.repository.OntologyRepositoryTest;
import org.zfin.orthology.OrthologyRepositoryTest;
import org.zfin.profile.repository.ProfileRepositoryTest;
import org.zfin.profile.service.ProfileServiceTest;
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
import org.zfin.sequence.reno.repository.SingleCandidateRepositoryTest;
import org.zfin.uniquery.SequenceIdListTest;
import org.zfin.webservice.MarkerSoapDbTest;
import org.zfin.wiki.service.AntibodyWikiWebServiceTest;


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
        DTOConversionServiceTest.class,
        CurationRPCTest.class,
        DatabaseServiceDbTest.class,
        DisplayGroupRepositoryTest.class,
        DTOConversionServiceTest.class,
        ExpressionRepositoryTest.class,
        ExpressionServiceTest.class,
        FeatureRepositoryTest.class,
        FigureRepositorySpec.class,
        FeatureServiceTest.class,
        FigureServiceTest.class,
        FigureViewServiceSpec.class,
        FishRepositoryTest.class,
        FishServiceTest.class,
        ConstructRepositoryTest.class,
        ConstructServiceTest.class,
        FpInferenceGafServiceTest.class,
        GafParserUnitTest.class,
        GafServiceTest.class,
        GBrowseServiceTest.class,
        GoaGafServiceTest.class,
        GoEvidenceTest.class,
        GwtConversionTest.class,
        ImageServiceSpec.class,
        InfrastructureRepositoryTest.class,
        LinkageRepositoryTest.class,
        MarkerRepositoryTest.class,
        MergeMarkerDBTest.class,
        MapAccessionDbLinkTest.class,
        MarkerGoTermEvidenceRepositoryTest.class,
        MarkerServiceTest.class,
        MarkerSoapDbTest.class,
        MutantRepositoryTest.class,
        OntologyManagerTest.class,
        OntologyRepositoryTest.class,
        OntologySerializationTest.class,
        OrthologyTest.class,
        OrthologyRepositoryTest.class,
        PaintGafServiceTest.class,
        PaginationResultTest.class,
        PhenotypeRepositoryTest.class,
        ProfileRepositoryTest.class,
        ProfileServiceTest.class,
        PublicationRepositoryTest.class,
        PhenotypeServiceDBTest.class,
        RenoRedundancyCandidateControllerTest.class,
        RenoMultiRunTest.class,
//        NomenclatureCandidateControllerTest.class, // no tests
        RenoRepositoryTest.class,
        SequenceRepositoryTest.class,
        TranscriptServiceTest.class,
        SequenceIdListTest.class,
        AntibodyWikiWebServiceTest.class,
        SingleCandidateRepositoryTest.class,
        TranscriptRepositoryTest.class,
        ZfinJSPFunctionsTest.class
//         MicroArrayTest.class     // Takes 1.5 min to run, but works.
})

public class DbUnitTests {

}
