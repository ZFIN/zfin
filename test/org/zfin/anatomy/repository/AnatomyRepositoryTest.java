package org.zfin.anatomy.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermAlias;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

public class AnatomyRepositoryTest extends AbstractDatabaseTest {

	/**
	 * Check that synonyms are not of group 'secondary id'
	 */
	@Test
	public void getAnatomyTermWithSynonyms() {
		String termName = "optic primordium";

		GenericTerm item = getOntologyRepository().getTermByName(termName, Ontology.ANATOMY);
		assertNotNull(item);
		Set<TermAlias> synonyms = item.getAliases();
		assertNotNull(synonyms);
		// check that none of the synonyms are secondary ids
		for (TermAlias syn : synonyms) {
			assertNotEquals(" Not a secondary id", syn.getAliasGroup().getName(), DataAliasGroup.Group.SECONDARY_ID.name());
		}
	}

	@Test
	public void getTotalNumberOfFiguresPerAnatomy() {
		// brain
		String termName = "retinal bipolar neuron";
		GenericTerm item = getOntologyRepository().getTermByName(termName, Ontology.ANATOMY);

		getPublicationRepository().getTotalNumberOfFiguresPerAnatomyItem(item);
		//assertEquals(1036, numOfFigures);

	}

	/**
	 * 1 - find anatomy item term
	 * 2 - find anatomy item term by synonym
	 * 3 - find anatomy item term not by data alias
	 */
	@Test
	public void getAnatomyItemsWithoutDataAlias() {
		// extrascapula
		String zdbID = "ZFA:0000663";
		GenericTerm term = getOntologyRepository().getTermByOboID(zdbID);
		assertNotNull(term);

		Set<TermAlias> synonyms = term.getAliases();
		assertTrue("Should be 1 or more synonym because filtered secondary", synonyms.size() >= 1);
	}

	@Test
	public void getDevelopmentStage() {
		// blastula:256-cell
		String zdbID = "ZFS:0000009";
		DevelopmentStage term = getAnatomyRepository().getStageByOboID(zdbID);
		assertNotNull(term);
	}

	@Test
	public void getAnatomyStatistics() {
		DevelopmentStage stage = new DevelopmentStage();
		stage.setZdbID("ZDB-STAGE-010723-1");
		DevelopmentStage adult = getAnatomyRepository().getStage(stage);

		List<AnatomyStatistics> statisticsList = getAnatomyRepository().getAnatomyItemStatisticsByStage(adult);
		assertNotNull(statisticsList);
	}

	@Test
	public void getAnatomyStatisticsByAo() {
		AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatistics("ZDB-TERM-120130-40");
		assertNotNull(statistics);
	}

	@Test
	public void getAnatomyMutantStatisticsByAo() {
		AnatomyStatistics statistics = getAnatomyRepository().getAnatomyStatisticsForMutants("ZDB-TERM-091209-10513");
		assertNotNull(statistics);
	}

	@Test
	public void getStageByStartHours() {
		DevelopmentStage developmentStage = getAnatomyRepository().getStageByStartHours(2.50F);
		assertNotNull(developmentStage);
		assertEquals("Blastula", developmentStage.getName(), "Blastula:256-cell");
	}

	@Test
	public void getStageByEndHours() {
		DevelopmentStage developmentStage = getAnatomyRepository().getStageByEndHours(30.00F);
		assertNotNull(developmentStage);
		assertEquals("Pharyngula:Prim-5", developmentStage.getName(), "Pharyngula:Prim-5");
	}

	@Test
	public void getMultipleTerms() {
		Set<String> ids = Set.of("ZFA:0001327", "ZFA:0000646");
		List<GenericTerm> termList = getAnatomyRepository().getMultipleTerms(ids);
		assertNotNull(termList);
		assertEquals(termList.size(), 2);
	}

	@Test
	public void stageOverlapTermsDevelopsInto() {
		// adaxial cell
		String termID = "ZDB-TERM-100331-3";
		double startHours = 10;
		double endHours = 144;
		List<GenericTerm> terms = getAnatomyRepository().getTermsDevelopingFromWithOverlap(termID, startHours, endHours);
		assertNotNull(terms);
		assertTrue(terms.size() > 0);
	}

	@Test
	public void getAnatomyItemStatisticsByStage() {
		// zygote
		String stageID = "ZDB-STAGE-010723-4";
		DevelopmentStage stage = getAnatomyRepository().getStageByID(stageID);
		List<AnatomyStatistics> stats = getAnatomyRepository().getAnatomyItemStatisticsByStage(stage);
		assertNotNull(stats);

	}
}
