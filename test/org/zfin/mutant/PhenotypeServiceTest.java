package org.zfin.mutant;

import org.junit.Before;
import org.junit.Test;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for service class that deals with Phenotype-related logic
 */
public class PhenotypeServiceTest {

    private GenericTerm cellAnatomyTerm;
    private GenericTerm anatomyTerm;
    private GenericTerm goTerm;
    private static String aoTermZdbID = "ZDB-ANAT-010921-586";
    private static String cellAOZdbID = "ZDB-ANAT-050915-158";
    private static String GOZdbID = "ZDB-GO-050915-158";
    private String muscleCell = "muscle cell";
    private static final String CILIUM = "cilium";
    private static final String qualityName = "white";
    private static final String qualityID = "ZDB-TERM-070117-324";


    @Before
    public void setUp() {
        anatomyTerm = new GenericTerm();
        anatomyTerm.setZdbID(aoTermZdbID);
        anatomyTerm.setTermName("pronephros");
        ////TODO
        //anatomyTerm.setCellTerm(false);

        cellAnatomyTerm = new GenericTerm();
        cellAnatomyTerm.setZdbID(cellAOZdbID);
        cellAnatomyTerm.setTermName("muscle cell");

        goTerm = new GenericTerm();
        goTerm.setZdbID(GOZdbID);
        goTerm.setTermName(CILIUM);
        //goTerm.setCellTerm(true);
    }

    @Test
    public void noPhenotypesOrAnatomy() {
        GenotypeExperiment genox = new GenotypeExperiment();
        Map<String, Set<String>> map = PhenotypeService.getPhenotypesGroupedByOntology(genox, anatomyTerm);
        assertTrue(map == null);

        map = PhenotypeService.getPhenotypesGroupedByOntology(null, anatomyTerm);
        assertTrue(map == null);

        map = PhenotypeService.getPhenotypesGroupedByOntology(genox, null);
        assertTrue(map == null);

    }

    /**
     * Single phenotype in AO.
     * Check correct ontology, single phenotype and term.
     */
    @Test
    public void singleAOPhenotype() {
        GenotypeExperiment genox = new GenotypeExperiment();
        PhenotypeExperiment phenoExp = new PhenotypeExperiment();
        String brightOrange = "bright orange";
        PhenotypeStatement pheno = createPhenotypeStatement(anatomyTerm, null, null, null, brightOrange);
        Set<PhenotypeStatement> phenotypes = new HashSet<PhenotypeStatement>(1);
        phenotypes.add(pheno);
        phenoExp.setPhenotypeStatements(phenotypes);
        Set<PhenotypeExperiment> phenoExperiments = new HashSet<PhenotypeExperiment>(1);
        phenoExperiments.add(phenoExp);
        genox.setPhenotypeExperiments(phenoExperiments);

        Map<String, Set<String>> map = PhenotypeService.getPhenotypesGroupedByOntology(genox, anatomyTerm);
        assertTrue(map != null);
        assertTrue(map.size() == 1);
        String terms = map.keySet().iterator().next();
        assertEquals(PhenotypeService.ANATOMY, terms);
        assertTrue(map.get(PhenotypeService.ANATOMY).size() == 1);
        Set desc = map.get(PhenotypeService.ANATOMY);
        assertTrue(desc.contains(brightOrange));
    }

    /**
     * Three phenotypes in AO.
     * Check correct ontology, three phenotype terms sorted by name
     */
    @Test
    public void ThreeAOPhenotype() {
        GenotypeExperiment genox = new GenotypeExperiment();
        String brightOrange = "bright orange";
        String pink = "pink";
        String angry = "angry";
        PhenotypeStatement statementOne = createPhenotypeStatement(anatomyTerm, null, null, null, brightOrange);
        PhenotypeStatement statementTwo = createPhenotypeStatement(anatomyTerm, null, null, null, pink);
        PhenotypeStatement statementThree = createPhenotypeStatement(anatomyTerm, null, null, null, angry);

        Set<PhenotypeStatement> phenotypes = new HashSet<PhenotypeStatement>(3);
        phenotypes.add(statementOne);
        phenotypes.add(statementTwo);
        phenotypes.add(statementThree);

        PhenotypeExperiment phenoExp = new PhenotypeExperiment();
        Set<PhenotypeExperiment> phenoExperiments = new HashSet<PhenotypeExperiment>(1);
        phenoExp.setPhenotypeStatements(phenotypes);
        phenoExperiments.add(phenoExp);
        genox.setPhenotypeExperiments(phenoExperiments);

        Map<String, Set<String>> map = PhenotypeService.getPhenotypesGroupedByOntology(genox, anatomyTerm);
        assertTrue(map != null);
        assertTrue(map.size() == 1);
        String terms = map.keySet().iterator().next();
        assertEquals(PhenotypeService.ANATOMY, terms);
        assertTrue(map.get(PhenotypeService.ANATOMY).size() == 3);
        Set desc = map.get(PhenotypeService.ANATOMY);
        Iterator iterator = desc.iterator();
        assertEquals(angry, iterator.next());
        assertEquals(brightOrange, iterator.next());
        assertEquals(pink, iterator.next());
    }


    /**
     * Nine phenotypes, three in AO, three in cell AO, and three in Go-cell component.
     * Check correct ontology, each of the three phenotype terms sorted by name,
     * overall grouped by:
     * 1) AO terms
     * 2) sorted by term name.
     */
    @Test
    public void ThreeAOThreeAOCellThreeGOPhenotype() {

        String brightOrange = "bright orange";
        String pink = "pink";
        String angry = "angry";

        PhenotypeStatement statementOne = createPhenotypeStatement(anatomyTerm, null, null, null, brightOrange);
        PhenotypeStatement statementTwo = createPhenotypeStatement(anatomyTerm, null, null, null, pink);
        PhenotypeStatement statementThree = createPhenotypeStatement(anatomyTerm, null, null, null, angry);

        Set<PhenotypeStatement> phenotypes = new HashSet<PhenotypeStatement>(3);
        phenotypes.add(statementOne);
        phenotypes.add(statementTwo);
        phenotypes.add(statementThree);

        String diastatic = "diastatic";
        String paleYellow = "pale yellow";
        String increasedSize = "increased size";
        PhenotypeStatement statementFour = createPhenotypeStatement(anatomyTerm, cellAnatomyTerm, null, null, diastatic);
        PhenotypeStatement statementFive = createPhenotypeStatement(anatomyTerm, cellAnatomyTerm, null, null, paleYellow);
        PhenotypeStatement statementSix = createPhenotypeStatement(anatomyTerm, cellAnatomyTerm, null, null, increasedSize);

        phenotypes.add(statementFour);
        phenotypes.add(statementFive);
        phenotypes.add(statementSix);

        String lightPurple = "light purple";
        String disorganized = "disorganized";
        String cystic = "cystic";
        PhenotypeStatement statementSeven = createPhenotypeStatement(anatomyTerm, goTerm, null, null, lightPurple);
        PhenotypeStatement statementEight = createPhenotypeStatement(anatomyTerm, goTerm, null, null, disorganized);
        PhenotypeStatement statementNine = createPhenotypeStatement(anatomyTerm, goTerm, null, null, cystic);

        phenotypes.add(statementSeven);
        phenotypes.add(statementEight);
        phenotypes.add(statementNine);

        GenotypeExperiment genox = new GenotypeExperiment();
        PhenotypeExperiment phenoExp = new PhenotypeExperiment();
        Set<PhenotypeExperiment> phenoExperiments = new HashSet<PhenotypeExperiment>(1);
        phenoExp.setPhenotypeStatements(phenotypes);
        phenoExperiments.add(phenoExp);
        genox.setPhenotypeExperiments(phenoExperiments);

        Map<String, Set<String>> map = PhenotypeService.getPhenotypesGroupedByOntology(genox, anatomyTerm);
        assertTrue(map != null);
        assertTrue(map.size() == 3);
        Iterator<String> termIterator = map.keySet().iterator();
        String terms = termIterator.next();
        assertEquals(PhenotypeService.ANATOMY, terms);
        assertTrue(map.get(PhenotypeService.ANATOMY).size() == 3);
        Set desc = map.get(PhenotypeService.ANATOMY);
        Iterator iterator = desc.iterator();
        assertEquals(angry, iterator.next());
        assertEquals(brightOrange, iterator.next());
        assertEquals(pink, iterator.next());

        terms = termIterator.next();
        assertEquals("cilium:pronephros", terms);
        assertTrue(map.get("cilium:pronephros").size() == 3);
        desc = map.get("cilium:pronephros");
        iterator = desc.iterator();
        assertEquals(cystic, iterator.next());
        assertEquals(disorganized, iterator.next());
        assertEquals(lightPurple, iterator.next());

        terms = termIterator.next();
        String name = muscleCell + ":pronephros";
        assertEquals(name, terms);
        assertTrue(map.get(name).size() == 3);
        desc = map.get(name);
        iterator = desc.iterator();
        assertEquals(diastatic, iterator.next());
        assertEquals(increasedSize, iterator.next());
        assertEquals(paleYellow, iterator.next());
    }

    private PhenotypeStatement createPhenotypeStatement(GenericTerm superTerm, GenericTerm subterm, GenericTerm relatedSuperterm, GenericTerm relatedSubterm, String qualityName) {
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(superTerm);
        entity.setSubterm(subterm);

        PhenotypeStatement statement = new PhenotypeStatement();
        statement.setEntity(entity);
        GenericTerm quality = new GenericTerm();
        quality.setTermName(qualityName);
        statement.setQuality(quality);
        return statement;
    }

}
