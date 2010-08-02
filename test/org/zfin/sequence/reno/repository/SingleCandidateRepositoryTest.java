package org.zfin.sequence.reno.repository;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;

import java.util.List;

public class SingleCandidateRepositoryTest {

    private static RenoRepository renoRepository = RepositoryFactory.getRenoRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    /**
     * Check that synonyms are not of group 'secondary id'
     */
    @Test
    public void getSingleRunCandidates() {
        // optic primordium
        String runZDB = "ZDB-RUN-071001-2";
        Run run = renoRepository.getRunByID(runZDB);

        List<RunCandidate> runCandidates = renoRepository.getSortedRunCandidates(run, "", 5);
        Assert.assertTrue(runCandidates != null);
    }

}
