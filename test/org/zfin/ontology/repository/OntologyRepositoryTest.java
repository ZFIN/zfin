package org.zfin.ontology.repository;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueResultException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionResult;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.PhenotypeStatement;
import org.zfin.ontology.*;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;

import java.util.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;


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

        Term termFromDatabase = ontologyRepository.getTermByName("left side", Ontology.SPATIAL);
        assertNotNull(termFromDatabase);
    }

    @Test
    public void getAnatomyRootTermInfo() {
        String anatomyRootID = "ZFA:0000037";
        Term term = ontologyRepository.getTermByOboID(anatomyRootID);
        Assert.assertNotNull(term);

        // Ensure obsoleted terms that do not have a term stage range are handled ok too, no exception
        anatomyRootID = "ZFA:0001160";
        term = ontologyRepository.getTermByOboID(anatomyRootID);
        term.getStart();
        Assert.assertNotNull(term);
    }

    @Test
    public void getTermWithDefinitionReference() {
        String anatomyRootID = "ZFA:0000089";
        GenericTerm term = ontologyRepository.getTermByOboID(anatomyRootID);
        assertNotNull(term);
        assertNotNull(term.getDefinitionReferences());
        assertTrue(term.getDefinitionReferences().size() > 0);
        assertNotNull(term.getDefinitionReferences().iterator().next().getForeignDB());
        boolean hasHttpExternalReference = false;
        for (TermDefinitionReference reference : term.getDefinitionReferences()) {
            if (reference.getForeignDB().getDbName() == ForeignDB.AvailableName.HTTP)
                hasHttpExternalReference = true;
        }
        assertTrue("Found HTTP external reference", hasHttpExternalReference);
    }

    @Test
    public void getGenericTermBrain() {
        // unfertilized egg"
        String oboID = "ZFA:0001570";
        Term term = ontologyRepository.getTermByOboID(oboID);
        DevelopmentStage endStage = getAnatomyRepository().getStageByName("Zygote:1-cell");
        DevelopmentStage startStage = getAnatomyRepository().getStageByName("Unknown");
        assertNotNull(startStage);
        assertNotNull(endStage);
        assertNotNull(term);
        assertEquals(endStage, term.getEnd());
        assertEquals(startStage, term.getStart());
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
        assertEquals("ontology name", "sequence", metadata.get(0).getName());
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


    /**
     * Not that the child terms include themselves.
     */
    @Test
    public void getChildrenTransitiveClosuresLineage() {
        GenericTerm term;
        List<TransitiveClosure> transitiveClosures;

        // http://www.berkeleybop.org/obo/tree/ZFA/ZFA:0000108?  // fin
        //  should be 200
        term = ontologyRepository.getTermByOboID("ZFA:0000108");
        transitiveClosures = ontologyRepository.getChildrenTransitiveClosures(term);

        assertThat(transitiveClosures.size(), greaterThan(180));
//        assertThat(transitiveClosures.size() , lessThan(201)); // was 200
        assertThat(transitiveClosures.size(), lessThan(400));

        // http://www.berkeleybop.org/obo/tree/ZFA/ZFA:0005299? // fin blood vessel . .. parent of perctoral fin blood vessel
        // should be 8
        term = ontologyRepository.getTermByOboID("ZFA:0005299");
        transitiveClosures = ontologyRepository.getChildrenTransitiveClosures(term);

        assertThat(transitiveClosures.size(), greaterThan(8));
//        assertThat(transitiveClosures.size() , lessThan(10)); // was 9, which I agree with 
        assertThat(transitiveClosures.size(), lessThan(20));

        // http://www.berkeleybop.org/obo/tree/ZFA/ZFA:0005096? // perctoral fin vaculature
        // should be 4
        term = ontologyRepository.getTermByOboID("ZFA:0005096");
        transitiveClosures = ontologyRepository.getChildrenTransitiveClosures(term);

        assertThat(transitiveClosures.size(), greaterThan(2));
//        assertThat(transitiveClosures.size() , lessThan(4)); // was 5
        assertThat(transitiveClosures.size(), lessThan(7));

        // http://www.berkeleybop.org/obo/tree/ZFA/ZFA:0005301? // pectoral fin blood vessel
        // should be 2
        term = ontologyRepository.getTermByOboID("ZFA:0005301");
        transitiveClosures = ontologyRepository.getChildrenTransitiveClosures(term);

        assertThat(transitiveClosures.size(), greaterThan(2));
//        assertThat(transitiveClosures.size() , lessThan(4)); // was 3, which is correct
        assertThat(transitiveClosures.size(), lessThan(7));


    }

    @Test
    public void getOntologyMetadata() throws Exception {
        List<OntologyMetadata> metadata = ontologyRepository.getAllOntologyMetadata();
        assertNotNull(metadata);
    }

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
        GenericTerm t = ontologyRepository.getTermByZdbID("ZDB-TERM-070117-118");
        assertThat(t.getChildTermRelationships().size(), greaterThan(4));
        assertThat(t.getChildTermRelationships().size(), lessThan(10));
        assertThat(t.getChildTerms().size(), greaterThan(4));
        assertThat(t.getChildTerms().size(), lessThan(10));
        assertEquals(t.getChildTermRelationships().size(), t.getChildTerms().size());
        assertEquals(1, t.getParentTerms().size());
        assertEquals(1, t.getParentTermRelationships().size());
        assertThat(t.getAllDirectlyRelatedTerms().size(), greaterThan(6));
        assertThat(t.getAllDirectlyRelatedTerms().size(), lessThan(10));

        for (GenericTermRelationship tr : t.getChildTermRelationships()) {
            assertEquals(t.getZdbID(), tr.getTermOne().getZdbID());
        }

        for (GenericTermRelationship tr : t.getParentTermRelationships()) {
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
    @Ignore("Disease ontology issue: uses disease_Ontology as a default namespace but then uses 'doid' " +
            "as namespaces for some terms. Need to check with the working group")
    public void getFirst2Terms() {
        List<String> allTerms = ontologyRepository.getFirstNTermsPerOntology(2);
        assertNotNull(allTerms);
        // BP, MF, CC, PATO, SM, ZFA, ZFS and the ominous pato.ontology
        // (which hopefully will go away soon)
        assertTrue(allTerms.size() > 15);
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
        List<Ontology> ontologies = new ArrayList<>(2);
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
        String termName = "bract formation";
        try {
            GenericTerm term = ontologyRepository.getTermByName(termName, Ontology.AOGO);
            assertNotNull(term);
        } catch (NonUniqueResultException e) {
            fail("Found more than one term with name '" + termName + "'");
        } catch (Exception e) {
            fail("An error occurred");

        }
    }

    /**
     * 'brain nucleus' is a parent term of 'dorsolateral motor nucleus of vagal nerve'
     */
    @Test
    public void parentChildRelationship() {
        GenericTerm brainNucleus = ontologyRepository.getTermByName("brain nucleus", Ontology.ANATOMY);
        GenericTerm subTerm = ontologyRepository.getTermByName("dorsolateral motor nucleus of vagal nerve", Ontology.ANATOMY);
        boolean isParentChildRelation = ontologyRepository.isParentChildRelationshipExist(brainNucleus, subTerm);
        assertTrue(isParentChildRelation);
    }

    @Test
    public void getDistinctRelationshipTypes() {
        List<String> relationshipTypes = ontologyRepository.getAllRelationships(Ontology.SO);
        assertNotNull(relationshipTypes);
    }

    @Test
    public void getTermsWithInvalidStageDefinition() {
        List<GenericTermRelationship> relationshipTypes = ontologyRepository.getTermsWithInvalidStartStageRange();
        assertNotNull(relationshipTypes);
        relationshipTypes = ontologyRepository.getTermsWithInvalidEndStageRange();
        assertNotNull(relationshipTypes);
        relationshipTypes = ontologyRepository.getTermsWithInvalidStartEndStageRangeForDevelopsFrom();
        assertNotNull(relationshipTypes);
    }

    @Test
    @Ignore("do not include in regular tests as it takes more than a minute")
    public void getExpressionAnnotationStageViolations() {
        List<ExpressionResult> expressionResultList = ontologyRepository.getExpressionResultsViolateStageRanges();
        assertNotNull(expressionResultList);
    }

    @Test
    public void getListOfMergedTermsInTermRelationship() {
        List<GenericTermRelationship> relationshipList = ontologyRepository.getTermRelationshipsWithMergedTerms();
        assertNotNull(relationshipList);
        List<GenericTerm> termList = ontologyRepository.getMergedTermsInTermRelationships();
        assertNotNull(termList);
    }

    @Test
    public void getTermsWithoutRelationships() {
        List<GenericTerm> relationshipList = ontologyRepository.getActiveTermsWithoutRelationships();
        assertNotNull(relationshipList);
    }

    @Test
    public void getTermByExample() {
        GenericTerm term = new GenericTerm();
        term.setOboID("ZFA:0000123");
        GenericTerm genericTerm = ontologyRepository.getTermByExample(term);
        assertNotNull(genericTerm);
        assertEquals("liver", genericTerm.getTermName());

        DevelopmentStage stage = new DevelopmentStage();
        stage.setAbbreviation("5-9 somites");
        stage = ontologyRepository.getStageByExample(stage);
        assertNotNull(genericTerm);
        assertEquals("ZFS:0000024", stage.getOboID());

    }

    @Test
    public void getNewRelationships() {
        Calendar date = Calendar.getInstance();
        date.set(2013, Calendar.APRIL, 15);
        List<GenericTermRelationship> relationships = ontologyRepository.getNewRelationships(date, Ontology.ANATOMY);
        assertNotNull(relationships);
    }

    @Test
    public void getRelationshipById() {
        GenericTermRelationship relationship = ontologyRepository.getRelationshipById("ZDB-TERMREL-110123-7220");
        assertNotNull(relationship);

    }

    @Test
    public void getOmimReferences() {
        // Usher Syndrome
        GenericTerm term = ontologyRepository.getTermByOboID("DOID:0050439");
        assertNotNull(term);
        Set<TermExternalReference> externalReferences = term.getExternalReferences();
        assertNotNull(externalReferences);

    }


}
