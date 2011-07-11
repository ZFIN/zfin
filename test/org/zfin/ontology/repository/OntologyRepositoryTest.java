package org.zfin.ontology.repository;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.ExpressionResult;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;


/**
 * Repository for Ontology-related actions: mostly lookup.
 */
public class OntologyRepositoryTest extends AbstractDatabaseTest {

    private final static Logger logger = Logger.getLogger(OntologyRepositoryTest.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    @Test
    public void getMatchingAliasAnatomyTerms() {
        List<TermAlias> anatomyList = ontologyRepository.getAllAliases(Ontology.ANATOMY);
        assertNotNull(anatomyList);
    }

    @Test
    public void getTermByName() {
        String anatomyTermName = "forerunner cell group";
        GenericTerm term = ontologyRepository.getTermByName(anatomyTermName, Ontology.ANATOMY);
        Assert.assertNotNull(term);

        Term termFromDatabase = ontologyRepository.getTermByName("left/right axis", Ontology.SPATIAL);
        assertNotNull(termFromDatabase);
    }

    @Test

    public void getAnatomyRootTermInfo() {
        String anatomyRootID = "ZFA:0000037";
        Term term = ontologyRepository.getTermByOboID(anatomyRootID);
        Assert.assertNotNull(term);
    }

    @Test
    public void loadAllTermsOfOntology() throws Exception {
        List<GenericTerm> terms = ontologyRepository.getAllTermsFromOntology(Ontology.QUALITY);
        Assert.assertNotNull(terms);
    }

    @Test
    public void checkTermSubset() throws Exception {
        Term term = ontologyRepository.getTermByName("fused with", Ontology.QUALITY);
        assertNotNull(term);
        Set<Subset> subs = term.getSubsets();
        assertNotNull(subs);
    }

    @Test
    public void loadOntologyMetatdataForAll() throws Exception {
        List<OntologyMetadata> metadata = ontologyRepository.getAllOntologyMetadata();
        assertNotNull(metadata);
        assertEquals("ontology name", "sequence ontology", metadata.get(0).getName());
        assertEquals("ontology name", "quality", metadata.get(1).getName());
        assertEquals("ontology name", "spatial", metadata.get(2).getName());
        assertEquals("ontology name", "zebrafish_anatomical_ontology", metadata.get(3).getName());
    }

    @Test
    public void loadOntologyMetatdataForQuality() throws Exception {
        OntologyMetadata metadata = ontologyRepository.getOntologyMetadata(Ontology.QUALITY.getOntologyName());
        assertNotNull(metadata);
        assertEquals("Default name space", "quality", metadata.getDefaultNamespace());
    }

    @Test
    public void getPhenotypesWithSecondaryTerms() throws Exception {
        List<PhenotypeStatement> phenotypesWithSecondaryTerms = ontologyRepository.getPhenotypesWithSecondaryTerms();
        assertNotNull(phenotypesWithSecondaryTerms);
    }

    @Test
    public void getChildrenTransitiveClosures() {
        String anatomyRootID = "ZFA:0000108"; // fin
        GenericTerm term = ontologyRepository.getTermByOboID(anatomyRootID);
        List<TransitiveClosure> transitiveClosures = ontologyRepository.getChildrenTransitiveClosures(term);
        assertTrue(transitiveClosures.size() > 199);
        assertTrue(transitiveClosures.size() < 400);
    }

    @Test
    public void getOntologyMetadata() throws Exception {
        List<OntologyMetadata> metadata = ontologyRepository.getAllOntologyMetadata();
        assertNotNull(metadata);
    }

    //    @Test
//    public void getTermRelationshipsForOntology(){
//        Map<String,List<TermRelationship>> relationshipStringListMap =
//                ontologyRepository.getTermRelationshipsForOntology(Ontology.QUALITY);
////        assertTrue(relationshipStringListMap.containsKey());
////        assertTrue(relationshipStringListMap.containsKey());
////        assertTrue(relationshipStringListMap.containsKey());
//
//
//
//    }
    @Test
    public void getFirst10Terms() {
        List<String> allTerms = ontologyRepository.getAllTerms(10);
        assertNotNull(allTerms);
        assertEquals(10, allTerms.size());
    }

    @Test
    public void getSubOntologyForTerm() {
        // a quality_processes term
        GenericTerm term = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-1367");
        assertNotSame(Ontology.QUALITY_PROCESSES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term));
        assertEquals(Ontology.QUALITY_QUALITIES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term));
        assertNotSame(Ontology.QUALITY, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term));
        assertNotSame(Ontology.GO_BP, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term));

        // a quality_qualities term
        GenericTerm term2 = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-1312");
        assertEquals(Ontology.QUALITY_PROCESSES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term2));
        assertNotSame(Ontology.QUALITY_QUALITIES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term2));
        assertNotSame(Ontology.QUALITY, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term2));
        assertNotSame(Ontology.GO_BP, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term2));

        // a quality term with neither as root
        // root is quality_qualitative, not process or quality objective process
        GenericTerm term3 = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-397");
        assertNotSame(Ontology.QUALITY_PROCESSES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term3));
        assertNotSame(Ontology.QUALITY_QUALITIES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term3));
        assertEquals(Ontology.QUALITY, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term3));
        assertNotSame(Ontology.GO_BP, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term3));

        // a GO term
        GenericTerm term4 = ontologyRepository.getTermByZdbID("ZDB-TERM-091209-10000"); // A GO Term
        assertNotSame(Ontology.QUALITY_PROCESSES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term4));
        assertNotSame(Ontology.QUALITY_QUALITIES, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term4));
        assertNotSame(Ontology.QUALITY, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term4));
        assertEquals(Ontology.GO_BP, ontologyRepository.getProcessOrPhysicalObjectQualitySubOntologyForTerm(term4));

    }


    @Test
    public void testRelationships() {
        // choose a term that has both children and parents
        // size
        Term t = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-118");
        assertThat(t.getChildTermRelationships().size(),greaterThan(4));
        assertThat(t.getChildTermRelationships().size(),lessThan(10));
        assertThat(t.getChildTerms().size(),greaterThan(4));
        assertThat(t.getChildTerms().size(),lessThan(10));
        assertEquals(t.getChildTermRelationships().size(),t.getChildTerms().size());
        assertEquals(1, t.getParentTerms().size());
        assertEquals(1, t.getParentTermRelationships().size());
        assertThat(t.getAllDirectlyRelatedTerms().size(),greaterThan(6));
        assertThat(t.getAllDirectlyRelatedTerms().size(),lessThan(10));

        for (TermRelationship tr : t.getChildTermRelationships()) {
            assertEquals(t.getZdbID(), tr.getTermOne().getZdbID());
        }

        for (TermRelationship tr : t.getParentTermRelationships()) {
            assertEquals(t.getZdbID(), tr.getTermTwo().getZdbID());
        }
    }

    @Test
    public void testRelationshipsForFastSearchWithRelationships() {
        Map<String, TermDTO> termMap = ontologyRepository.getTermDTOsFromOntology(Ontology.SPATIAL);
        int count = ontologyRepository.getNumberTermsForOntology(Ontology.SPATIAL);
        assertEquals(count, termMap.size());

        TermDTO postTermDTO = termMap.get("ZDB-TERM-100722-134"); // posterial margin
        assertNotNull(postTermDTO);
        TermDTO antTermDTO = termMap.get("ZDB-TERM-100722-8"); // anatomical margin
        assertNotNull(antTermDTO);

        // make sure we get the proper parent
        Set<TermDTO> parentTerms = postTermDTO.getParentTerms();
        assertNotNull(parentTerms);
        assertTrue(parentTerms.size() >= 1);

        // make sure we have the proper children
        Set<TermDTO> childTermDTOs = antTermDTO.getChildrenTerms();
        assertNotNull(childTermDTOs);
        assertEquals(22, childTermDTOs.size());

        boolean hasTerm = false;
        for (TermDTO termDTO : childTermDTOs) {
            if (termDTO.getZdbID().equals(postTermDTO.getZdbID())) {
                hasTerm = true;
            }
        }
        assertTrue(hasTerm);

        assertEquals(2, antTermDTO.getAliases().size());
    }


    @Test
    public void loadOntologies() {
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        assertEquals(ontologyRepository.getNumberTermsForOntology(Ontology.STAGE), ontologyRepository.getTermDTOsFromOntology(Ontology.STAGE).size());
        endTime = System.currentTimeMillis();
        logger.info("STAGE time End: " + (endTime - startTime) / 1000f + "s");


        startTime = System.currentTimeMillis();
        assertEquals(ontologyRepository.getNumberTermsForOntology(Ontology.SPATIAL), ontologyRepository.getTermDTOsFromOntology(Ontology.SPATIAL).size());
        endTime = System.currentTimeMillis();
        logger.info("SPATIAL time End: " + (endTime - startTime) / 1000f + "s");


//        startTime = System.currentTimeMillis();
//        assertEquals(ontologyRepository.getNumberTermsForOntology(Ontology.QUALITY),ontologyRepository.getTermDTOsFromOntology(Ontology.QUALITY).size());
//        endTime = System.currentTimeMillis();
//        logger.info("QUALITY time End: " + (endTime - startTime) / 1000f + "s");

//        startTime = System.currentTimeMillis();
//        Map<String,TermDTO> anatomyTermDtos = ontologyRepository.getTermDTOsFromOntology(Ontology.ANATOMY);
//        assertEquals(ontologyRepository.getNumberTermsForOntology(Ontology.ANATOMY),anatomyTermDtos.keySet().size());
//        endTime = System.currentTimeMillis();
//        logger.info("ANATOMY time End: " + (endTime - startTime) / 1000f + "s");

    }

    @Test
    public void getAllChildZdbIDs() {
        Set<String> childZdbIDs = ontologyRepository.getAllChildZdbIDs("ZDB-TERM-070117-1242");
        assertTrue(childZdbIDs.size() > 1000);
        assertTrue(childZdbIDs.size() < 2000);
    }

    @Test
    public void getFirst2Terms() {
        List<String> allTerms = ontologyRepository.getFirstNTermsPerOntology(2);
        assertNotNull(allTerms);
        // BP, MF, CC, PATO, SM, ZFA, ZFS and the ominous pato.ontology
        // (which hopefully will go away soon)
        assertEquals(18, allTerms.size());
    }

    @Test
    public void getReplacedByTerm() {
        // obsoleted term with a replaced_by term suggestion
        // repairsome
        GenericTerm obsoletedTerm = ontologyRepository.getTermByOboID("GO:0000108");
        List<ReplacementTerm> allTerms = ontologyRepository.getReplacedByTerms(obsoletedTerm);
        assertNotNull(allTerms);
        assertEquals(1, allTerms.size());
    }

    @Test
    public void getConsiderTerms() {
        // obsoleted term with three consider terms
        // nucleosome modeling
        GenericTerm obsoletedTerm = ontologyRepository.getTermByOboID("GO:0016583");
        List<ConsiderTerm> allTerms = ontologyRepository.getConsiderTerms(obsoletedTerm);
        assertNotNull(allTerms);
        assertEquals(1, allTerms.size());
    }

    @Test
    public void lookupByTermNameExcludesSecondaryTerms() {
        // PATO term that exists as a secondary term in our term table.
        // ensure that only one term is retrieved.
        String termName = "spatial pattern";
        try {
            GenericTerm term = ontologyRepository.getTermByName(termName, Ontology.QUALITY_QUALITIES);
            assertNotNull(term);
        } catch (NonUniqueResultException e) {
            fail("Found more than one term with name '" + termName + "'");
        } catch (Exception e) {
            fail("An error occurred");

        }
        List<Ontology> ontologies = new ArrayList<Ontology>(2);
        ontologies.add(Ontology.QUALITY);
        ontologies.add(Ontology.GO);
        try {
            GenericTerm term = ontologyRepository.getTermByName(termName, ontologies);
            assertNotNull(term);
        } catch (NonUniqueResultException e) {
            fail("Found more than one term with name '" + termName + "'");
        } catch (Exception e) {
            fail("An error occurred");
        }
    }

    @Test
    public void getTermByNameForComposedOntologies() {
        String termName = "retinal isomerase activity";
        try {
            GenericTerm term = ontologyRepository.getTermByName(termName, Ontology.GO_BP_MF);
            assertNotNull(term);
        } catch (NonUniqueResultException e) {
            fail("Found more than one term with name '" + termName + "'");
        } catch (Exception e) {
            fail("An error occurred");

        }
    }

    @Test
    public void getGoEvidenceOnSecondaryTerms() {
        List<MarkerGoTermEvidence> term = ontologyRepository.getGoEvidenceOnSecondaryTerms();
        assertTrue(term == null || term.size() == 0);
    }

    @Test
    public void getExpressionsOnSecondaryTerms() {
        List<ExpressionResult> term = ontologyRepository.getExpressionsOnSecondaryTerms();
        assertTrue(term == null || term.size() == 0);
    }

    @Test
    public void getPhenotypesOnSecondaryTerms() {
        List<PhenotypeStatement> term = ontologyRepository.getPhenotypesOnSecondaryTerms();
        assertTrue(term == null || term.size() == 0);
    }
}
