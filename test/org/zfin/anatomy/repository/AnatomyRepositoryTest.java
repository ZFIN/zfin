package org.zfin.anatomy.repository;

import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.*;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

public class AnatomyRepositoryTest extends AbstractDatabaseTest{

    /**
     * Check that synonyms are not of group 'secondary id'
     */
    @Test
    public void getAnatomyTermWithSynonyms() {
        // optic primordium
        String termName = "optic primordium";

        AnatomyItem item = getAnatomyRepository().getAnatomyItem(termName);
        assertTrue(item != null);
        Set<AnatomySynonym> synonyms = item.getSynonyms();
        assertTrue(synonyms != null);
        // check that none of the synonyms are secondary ids
        for (AnatomySynonym syn : synonyms) {
            assertEquals(" Not a secondary id", true, syn.getGroup() != DataAliasGroup.Group.SECONDARY_ID);
        }
    }

    @Test
    public void getTotalNumberOfFiguresPerAnatomy() {
        // brain
        String termName = "brain";
        Term item = getOntologyRepository().getTermByName(termName, Ontology.ANATOMY);

        getPublicationRepository().getTotalNumberOfFiguresPerAnatomyItem(item);
        //assertEquals(1036, numOfFigures);

    }

    @Test
    public void getAnatomyRelationships() {
        String termName = "neural rod";
        AnatomyItem item = getAnatomyRepository().getAnatomyItem(termName);

        List<AnatomyRelationship> relatedTerms = item.getAnatomyRelations();
        assertTrue(relatedTerms != null);

    }

    @Test
    public void getAnatomyTermsSearchResult() {
        String searchTerm = "bra";

        List<AnatomyItem> terms = getAnatomyRepository().getAnatomyItemsByName(searchTerm, true);
        assertNotNull(terms);
    }

    @Test
    public void compareWildTypeSelectionToFullForMorphs() {
        Term item = getOntologyRepository().getTermByName("neural plate", Ontology.ANATOMY);
        PaginationResult<GenotypeExperiment> genotypeWildtype = getMutantRepository().getGenotypeExperimentMorpholinos(item, true, null);
        PaginationResult<GenotypeExperiment> genotypeNonWildtype = getMutantRepository().getGenotypeExperimentMorpholinos(item, false, null);

        assertNotNull(genotypeWildtype.getPopulatedResults());
        assertNotNull(genotypeNonWildtype.getPopulatedResults());
        assertNotSame("It is feasible, but unlikely that these will ever be the same", genotypeWildtype.getTotalCount(), genotypeNonWildtype.getTotalCount()); // its feasible, but not likely


    }

    @Test
    public void getWildtypeMorpholinos() {
        // String neuralPlateZdbID = "ZDB-ANAT-010921-560";
        Term item = getOntologyRepository().getTermByName("neural plate", Ontology.ANATOMY);
        PaginationResult<GenotypeExperiment> genos = getMutantRepository().getGenotypeExperimentMorpholinos(item, true, null);
        assertNotNull(genos.getPopulatedResults());
        assertTrue(genos.getPopulatedResults().size() > 1);

        List<GenotypeExperiment> genotypeList = getMutantRepository().getGenotypeExperimentMorpholinos(item, true);
        assertNotNull(genotypeList);
        assertTrue(genotypeList.size() > 1);

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
        String zdbID = "ZDB-ANAT-011113-588";
        List<AnatomyItem> terms;
        AnatomyItem term = getAnatomyRepository().getAnatomyTermByID(zdbID);
        assertNotNull(term);

        Set<AnatomySynonym> synonyms = term.getSynonyms();
        assertTrue("Should be 1 or more synonym because filtered secondary", synonyms.size() >= 1);

        // 2- get by synonym
        terms = getAnatomyRepository().getAnatomyItemsByName("supratemporal", false);
        assertNotNull(terms);
        assertTrue(terms.size() > 2);

        // 3- get by data alias
        terms = getAnatomyRepository().getAnatomyItemsByName("413", false);
        assertNotNull(terms);
        assertTrue("Should be no terms for '413'", terms.isEmpty());
    }

    @Test
    public void stageOverlapTermsDevelopsInto() {
        // adaxial cell
        String zdbID = "ZDB-ANAT-010921-408";
        double startHours = 36;
        double endHours = 144;
        List<AnatomyItem> terms = getAnatomyRepository().getTermsDevelopingFromWithOverlap(zdbID, startHours, endHours);
        assertTrue(terms != null);
        assertEquals(1, terms.size());
        // adaxial cell develops from
        assertEquals("migratory slow muscle precursor cell", terms.get(0).getName());
    }

    @Test
    public void stageOverlapTermsDevelopsFrom() {
        // slow muscle develops from myotome,
        // range: 11.66-14
        String zdbID = "ZDB-ANAT-031211-15";
        double startHours = 0;
        double endHours = 10.5;
        List<AnatomyItem> terms = getAnatomyRepository().getTermsDevelopingIntoWithOverlap(zdbID, startHours, endHours);
        assertTrue(terms != null);
        assertEquals(3, terms.size());
        // adaxial cell develops from
        assertEquals("migratory slow muscle precursor cell", terms.get(0).getName());
        assertEquals("myotome", terms.get(1).getName());
        assertEquals("slow muscle myoblast", terms.get(2).getName());
    }

    @Test
    public void getStartStageByOboId() {
        // brain
        String aoOboID = "ZFA:0000008";
        DevelopmentStage stage = getAnatomyRepository().getStartStage(aoOboID);
        assertNotNull(stage);
        assertEquals("ZDB-STAGE-020626-1", stage.getZdbID());
    }

    @Test
    public void getEndStageByOboId() {
        // brain
        String aoOboID = "ZFA:0000008";
        DevelopmentStage stage = getAnatomyRepository().getEndStage(aoOboID);
        assertNotNull(stage);
        assertEquals("ZDB-STAGE-010723-39", stage.getZdbID());
    }

    @Test
    public void getSubstructureAntibodies() {
        String aoTermName = "cranium";
        Term term = new GenericTerm();
        term.setID("ZDB-TERM-100331-706");
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
    public void getAnatomyItemStatisticsByStage(){
        // zygote
        String stageID = "ZDB-STAGE-010723-4";
        DevelopmentStage stage = getAnatomyRepository().getStageByID(stageID);
        List<AnatomyStatistics> stats = getAnatomyRepository().getAnatomyItemStatisticsByStage(stage);
        assertNotNull(stats);

    }
}
