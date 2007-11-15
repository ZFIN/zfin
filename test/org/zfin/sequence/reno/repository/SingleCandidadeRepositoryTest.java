package org.zfin.sequence.reno.repository;

import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomySynonym;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.sequence.reno.RunCandidate;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Set;
import java.util.List;

public class SingleCandidadeRepositoryTest {

    private static RenoRepository renoRepository = RepositoryFactory.getRenoRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }

    /**
     * Check that synonyms are not of group 'seconday id'
     */
    @Test
    public void getSingleRunCnadidates() {
        // optic primordium
        String runZDB = "ZDB-RUN-071001-2";

        List<RunCandidate> runCandidates = renoRepository.getSortedRunCandidates(runZDB, "", 5 );
        Assert.assertTrue(runCandidates != null);
    }

}
