package org.zfin.anatomy.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.ontology.*;

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
        // optic primordium
        String termName = "optic primordium";

        Term item = getOntologyRepository().getTermByName(termName, Ontology.ANATOMY);
        assertTrue(item != null);
        Set<TermAlias> synonyms = item.getAliases();
        assertNotNull(synonyms);
        // check that none of the synonyms are secondary ids
        for (TermAlias syn : synonyms) {
            assertEquals(" Not a secondary id", true, syn.getGroup() != DataAliasGroup.Group.SECONDARY_ID);
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

    @Test
    public void getAnatomyRelationships() {
        String termName = "neural rod";
        Term item = getOntologyRepository().getTermByName(termName, Ontology.ANATOMY);
        List<GenericTermRelationship> anatomyRelationships = getOntologyRepository().getTermRelationships(item);
        assertNotNull(anatomyRelationships);
        assertTrue(anatomyRelationships.size() > 0);

    }

    /**
     * 1 - find anatomy item term
     * 2 - find anatomy item term by synonym
     * 3 - find anatomy item term not by data alias
     */
    @Test
    public void getAnatomyItemsWithoutDataAlias() {
        // 1 - get by name
        // extrascapula
        String zdbID = "ZFA:0000663";
        Term term = getOntologyRepository().getTermByOboID(zdbID);
        assertNotNull(term);

        Set<TermAlias> synonyms = term.getAliases();
        assertTrue("Should be 1 or more synonym because filtered secondary", synonyms.size() >= 1);

    }

    @Test
    public void stageOverlapTermsDevelopsInto() {
        // adaxial cell
        String oboID = "ZFA:0000003";
        String termID = "ZDB-TERM-100331-3";
        double startHours = 10;
        double endHours = 144;
        List<GenericTerm> terms = getAnatomyRepository().getTermsDevelopingFromWithOverlap(termID, startHours, endHours);
        assertTrue(terms != null);
        assertTrue(terms.size() > 0);
    }

    @Test
    public void getSubstructureAntibodies() {
        String aoTermName = "cranium";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-706");
        term.setTermName(aoTermName);
        assertNotNull(term);

        // only primary ao term
        getAntibodyRepository().getAntibodiesByAOTerm(term, new PaginationBean(), false);
//        assertEquals("no antibodies annotated against cranium", 0, antibodies.getPopulatedResults().size());

        // include annotation to substructures
        getAntibodyRepository().getAntibodiesByAOTerm(term, new PaginationBean(), true);
//        assertTrue("no antibodies annotated against cranium", antibodies.getPopulatedResults().size() > 0);


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
