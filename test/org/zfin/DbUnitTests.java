package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.repository.AnatomyRepositoryTest;
import org.zfin.antibody.repository.AntibodyRepositoryTest;
import org.zfin.datatransfer.doi.DOITest;
import org.zfin.expression.repository.ExpressionRepositoryTest;
import org.zfin.gwt.curation.ui.CurationRPCTest;
import org.zfin.gwt.marker.GoEvidenceTest;
import org.zfin.infrastructure.EnumValidationTest;
import org.zfin.infrastructure.InfrastructureRepositoryTest;
import org.zfin.mapping.repository.LinkageRepositoryTest;
import org.zfin.marker.MergeMarkerTest;
import org.zfin.marker.repository.MarkerRepositoryTest;
import org.zfin.marker.repository.TranscriptRepositoryTest;
import org.zfin.mutant.MutantRepositoryTest;
import org.zfin.orthology.OrthologyRepositoryTest;
import org.zfin.people.repository.PeopleRepositoryTest;
import org.zfin.publication.repository.PublicationRepositoryTest;
import org.zfin.sequence.DisplayGroupRepositoryTest;
import org.zfin.sequence.MapAccessionDbLinkTest;
import org.zfin.sequence.SequenceRepositoryTest;
import org.zfin.sequence.TranscriptServiceTest;
import org.zfin.sequence.blast.BlastRepositoryTest;
import org.zfin.sequence.reno.AlignmentsControllerTest;
import org.zfin.sequence.reno.MultiRunTest;
import org.zfin.sequence.reno.OrthologyTest;
import org.zfin.sequence.reno.RenoRepositoryTest;
import org.zfin.sequence.reno.presentation.RedundancyCandidateControllerTest;
import org.zfin.sequence.reno.repository.SingleCandidateRepositoryTest;
import org.zfin.util.BODtoConversionServiceTest;

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
        BODtoConversionServiceTest.class,
        CurationRPCTest.class,
        InfrastructureRepositoryTest.class,
        MarkerRepositoryTest.class,
        MergeMarkerTest.class,
        TranscriptServiceTest.class,
        OrthologyTest.class,
        MutantRepositoryTest.class,
        PaginationResultTest.class,
        PeopleRepositoryTest.class,
        PublicationRepositoryTest.class,
        RedundancyCandidateControllerTest.class,
//        NomenclatureCandidateControllerTest.class, // no tests
        RenoRepositoryTest.class,
        SequenceRepositoryTest.class,
        MapAccessionDbLinkTest.class,
        EnumValidationTest.class,
        MultiRunTest.class,
        ExpressionRepositoryTest.class,
        OrthologyRepositoryTest.class,
        LinkageRepositoryTest.class,
        SingleCandidateRepositoryTest.class,
        TranscriptRepositoryTest.class,
        DisplayGroupRepositoryTest.class,
        GoEvidenceTest.class,
        BlastRepositoryTest.class
//         MicroArrayTest.class     // Takes 1.5 min to run, but works.
})

public class DbUnitTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(DbUnitTests.class);
    }
}
