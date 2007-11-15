package org.zfin.anatomy;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.zfin.BaseDatabaseUnitTest;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

public class DevelopmentStageTest extends BaseDatabaseUnitTest {

    private static AnatomyRepository ar ;

    public static void main(String args[]) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(DevelopmentStageTest.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ar = RepositoryFactory.getAnatomyRepository();
    }

    /**
     * Test that all development stages are in the mock obeject.
     */
    public void testAllStages() {
        List stages = ar.getAllStages();
        assertEquals("All Stages", 45, stages.size());
    }

    /**
     * Test display names for the developmental stage.
     * It is a concatenation of name, start and stop and other feature attribute.
     * See DevelopmentStage for more info.
     */
    public void testDisplayStages() {
        List stages = ar.getAllStages();
        assertEquals("All Stages", 45, stages.size());

        DevelopmentStage stage = new DevelopmentStage();
        stage.setZdbID("ZDB-STAGE-010723-39");
        DevelopmentStage adult = ar.getStage(stage);

        String adultDisplay = StagePresentation.createDisplayEntry(adult);
        assertEquals("Adult Stage Display", "Adult (90d-730d, breeding adult)", adultDisplay);

        stage.setZdbID("ZDB-STAGE-050211-1");
        adult = ar.getStage(stage);
        String unknownDisplay = StagePresentation.createDisplayEntry(adult);
        assertEquals("Unknown Stage Display", "Unknown", unknownDisplay);

        // test if no stage object is available
        String nullStage = StagePresentation.createDisplayEntry(null);
        assertNull("No Stage object", nullStage);
    }

    public void testabbreviation(){

    }
}
