package org.zfin;

import junit.framework.JUnit4TestAdapter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zfin.anatomy.repository.AnatomyRepositoryTest;
import org.zfin.infrastructure.InfrastructureRepositoryTest;
import org.zfin.mutant.MutantRepositoryTest;
import org.zfin.people.repository.PeopleRepositoryTest;
import org.zfin.sequence.MapAccessionDbLinkTest;
import org.zfin.sequence.SequenceRepositoryTest;
import org.zfin.sequence.blast.BlastRepositoryTest;
import org.zfin.sequence.reno.AlignmentsControllerTest;
import org.zfin.sequence.reno.presentation.RedundancyCandidateControllerTest;
import org.zfin.sequence.reno.presentation.NomenclatureCandidateControllerTest;
import org.zfin.sequence.reno.OrthologyTest;
import org.zfin.sequence.reno.RenoRepositoryTest;
import org.zfin.sequence.reno.MultiRunTest;
import org.zfin.publication.PublicationRepositoryTest;
import org.zfin.marker.repository.MarkerRepositoryTest;
import org.zfin.datatransfer.MicroArrayTest;
import org.zfin.infrastructure.EnumValidationTest;

/**
 * This is the master unit test class that runs all registered unit tests (suite)
 * that require a database connection.
 * Add your new unit test here if it is ready to be included in regular builds.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AlignmentsControllerTest.class,
        AnatomyRepositoryTest.class,
        BlastRepositoryTest.class,
        InfrastructureRepositoryTest.class,
        MarkerRepositoryTest.class,
        MutantRepositoryTest.class,
        OrthologyTest.class,
        PeopleRepositoryTest.class,
        PublicationRepositoryTest.class,
        RedundancyCandidateControllerTest.class,
        NomenclatureCandidateControllerTest.class,
        RenoRepositoryTest.class,
        SequenceRepositoryTest.class,
        MapAccessionDbLinkTest.class,
        EnumValidationTest.class,
        MultiRunTest.class
//        ,MicroArrayTest.class     // Takes 1.5 min to run, but works.
        })

public class DbUnitTests {

    public static junit.framework.Test suite() {
        TestConfiguration.configure();
        return new JUnit4TestAdapter(DbUnitTests.class);
    }
}
