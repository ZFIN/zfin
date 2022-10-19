package org.zfin.anatomy;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.presentation.StagePresentation;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.*;

public class DevelopmentStageTest extends AbstractDatabaseTest {

	private static final AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();

	/**
	 * Test that all development stages are in the mock object.
	 */
	@Test
	public void testAllStages() {
		List<DevelopmentStage> stages = aoRepository.getAllStages();
		assertEquals("All Stages", 45, stages.size());
	}

	@Test
	public void testAllStagesWithoutUnknown() {
		List<DevelopmentStage> stages = aoRepository.getAllStagesWithoutUnknown();
		assertEquals("All Stages", 44, stages.size());
	}

	/**
	 * Test display names for the developmental stage.
	 * It is a concatenation of name, start and stop and other feature attribute.
	 * See DevelopmentStage for more info.
	 */
	@Test
	public void testDisplayStages() {
		List<DevelopmentStage> stages = aoRepository.getAllStages();
		assertEquals("All Stages", 45, stages.size());

		DevelopmentStage stage = new DevelopmentStage();
		stage.setZdbID("ZDB-STAGE-010723-39");
		DevelopmentStage adult = aoRepository.getStage(stage);

		String adultDisplay = StagePresentation.createDisplayEntry(adult);
		assertEquals("Adult Stage Display", "Adult (90d-730d, breeding adult)", adultDisplay);

		stage.setZdbID("ZDB-STAGE-050211-1");
		adult = aoRepository.getStage(stage);
		String unknownDisplay = StagePresentation.createDisplayEntry(adult);
		assertEquals("Unknown Stage Display", "Unknown", unknownDisplay);

		// test if no stage object is available
		assertNull("No Stage object", StagePresentation.createDisplayEntry(null));
	}

	/**
	 * Check that Zygote is an earlier stage than Blastula.
	 */
	@Test
	public void stageComparisonRegular() {
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
	public void unknownStageEarlierThanAnyOther() {
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
	public void compareTheSameStages() {
		String zygoteName = "Zygote:1-cell";
		DevelopmentStage zygote = aoRepository.getStageByName(zygoteName);
		assertFalse("Zygote does not come before Zygote", zygote.earlierThan(zygote));
	}
}
