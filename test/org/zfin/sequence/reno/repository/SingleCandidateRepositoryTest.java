package org.zfin.sequence.reno.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class SingleCandidateRepositoryTest extends AbstractDatabaseTest {

    private static final RenoRepository renoRepository = RepositoryFactory.getRenoRepository();

    /**
     * Check that synonyms are not of group 'secondary id'
     */
    @Test
    public void getSingleRunCandidates() {
        // optic primordium
        String runZDB = "ZDB-RUN-071001-2";
        Run run = renoRepository.getRunByID(runZDB);

        List<RunCandidate> runCandidates = renoRepository.getSortedRunCandidates(run, "", 5);
        assertNotNull(runCandidates);
    }

}
