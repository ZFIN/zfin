package org.zfin.anatomy;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.*;

public class DevelopmentStageTest  {

    private static AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();

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
     * Test that all development stages are in the mock obeject.
     */
    @Test
    public void testAllStages() {
        List stages = aoRepository.getAllStages();
        assertEquals("All Stages", 45, stages.size());
    }

    /**
     * Test display names for the developmental stage.
     * It is a concatenation of name, start and stop and other feature attribute.
     * See DevelopmentStage for more info.
     */
    @Test
    public void testDisplayStages() {
        List stages = aoRepository.getAllStages();
        assertEquals("All Stages", 45, stages.size());

        DevelopmentStage stage = new DevelopmentStage();
        stage.setZdbID("ZDB-STAGE-010723-39");
        DevelopmentStage adult = aoRepository.getStage(stage);

        String adultDisplay = StagePresentation.createDisplayEntry(adult);
        assertEquals("Adult Stage Display", "Adult (90d-730d, breeding adult)", adultDisplay);

        stage.setZdbID("ZDB-STAGE-050211-1");
        adult = aoRepository.getStage(stage);
        String unknownDisplay = StagePresentation.createDisplayEntry(adult);
        assertEquals("Unknown Stage Display", "Unknown (0.0h-730d)", unknownDisplay);

        // test if no stage object is available
        String nullStage = StagePresentation.createDisplayEntry(null);
        assertNull("No Stage object", nullStage);
    }

    /**
     * Check that Zygote is an earlier stage than Blastula.
     */
    @Test
    public void stageComparisonRegular(){
        String zygoteName = "Zygote:1-cell";
        DevelopmentStage zygote = aoRepository.getStageByName(zygoteName);

        String blastulaName = "Blastula:128-cell";
        DevelopmentStage blastula = aoRepository.getStageByName(blastulaName);

        assertTrue("Zygote comes before blastula", zygote.earlierThan(blastula));

    }

    /**
     * Check that Unknown is earlier than any other stage.
     */
    @Test
    public void unknownStageEarlierThanAnyOther(){
        String zygoteName = "Zygote:1-cell";
        DevelopmentStage zygote = aoRepository.getStageByName(zygoteName);

        String unknownName = "Unknown";
        DevelopmentStage unknown = aoRepository.getStageByName(unknownName);

        assertTrue("Unknown comes before Zygote", unknown.earlierThan(zygote));

    }

    /**
     * Check that two same stages produce a false;
     */
    @Test
    public void compareTheSameStages(){
        String zygoteName = "Zygote:1-cell";
        DevelopmentStage zygote = aoRepository.getStageByName(zygoteName);

        assertTrue("Zygote does not come before Zygote", !zygote.earlierThan(zygote));

    }
}
