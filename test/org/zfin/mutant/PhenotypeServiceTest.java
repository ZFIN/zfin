package org.zfin.mutant;

import org.junit.Before;
import org.junit.Test;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

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

    private Term cellAnatomyTerm;
    private Term anatomyTerm;
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
        //cellAnatomyTerm.setCellTerm(true);
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
        Phenotype pheno = new Phenotype();
        pheno.setSuperterm(anatomyTerm);
        GenericTerm term = new GenericTerm();
        String brightOrange = "bright orange";
        term.setTermName(brightOrange);
        pheno.setTerm(term);
        Set<Phenotype> phenotypes = new HashSet<Phenotype>(1);
        phenotypes.add(pheno);
        genox.setPhenotypes(phenotypes);

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
        Phenotype pheno = new Phenotype();
        pheno.setSuperterm(anatomyTerm);
        GenericTerm term = new GenericTerm();
        String brightOrange = "bright orange";
        term.setTermName(brightOrange);
        pheno.setTerm(term);

        Phenotype phenoTwo = new Phenotype();
        phenoTwo.setSuperterm(anatomyTerm);
        GenericTerm termTwo = new GenericTerm();
        String pink = "pink";
        termTwo.setTermName(pink);
        phenoTwo.setTerm(termTwo);

        Phenotype phenoThree = new Phenotype();
        phenoThree.setSuperterm(anatomyTerm);
        GenericTerm termThree = new GenericTerm();
        String angry = "angry";
        termThree.setTermName(angry);
        phenoThree.setTerm(termThree);

        Set<Phenotype> phenotypes = new HashSet<Phenotype>(3);
        phenotypes.add(pheno);
        phenotypes.add(phenoTwo);
        phenotypes.add(phenoThree);
        genox.setPhenotypes(phenotypes);

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
     * Nine phenotypes, three in AO, three in cell AO, and three in Go-cellcomponent
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

        Set<Phenotype> phenotypes = new HashSet<Phenotype>();
        phenotypes.add(getAOPhenotype(brightOrange));
        phenotypes.add(getAOPhenotype(pink));
        phenotypes.add(getAOPhenotype(angry));

        String diastatic = "diastatic";
        String paleYellow = "pale yellow";
        String increasedSize = "increased size";
        phenotypes.add(getCellAOPhenotype(diastatic));
        phenotypes.add(getCellAOPhenotype(paleYellow));
        phenotypes.add(getCellAOPhenotype(increasedSize));

        String lightPurple = "light purple";
        String disorganized = "disorganized";
        String cystic = "cystic";
        phenotypes.add(getGOPhenotype(lightPurple));
        phenotypes.add(getGOPhenotype(disorganized));
        phenotypes.add(getGOPhenotype(cystic));

        GenotypeExperiment genox = new GenotypeExperiment();
        genox.setPhenotypes(phenotypes);

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
        final String name = "pronephros:" + muscleCell;
        assertEquals(name, terms);
        assertTrue(map.get(name).size() == 3);
        desc = map.get(name);
        iterator = desc.iterator();
        assertEquals(diastatic, iterator.next());
        assertEquals(increasedSize, iterator.next());
        assertEquals(paleYellow, iterator.next());
    }

    private Phenotype getAOPhenotype(String phenotypeName) {
        Phenotype pheno = new Phenotype();
        pheno.setSuperterm(anatomyTerm);
        GenericTerm term = new GenericTerm();
        term.setTermName(phenotypeName);
        pheno.setTerm(term);
        return pheno;
    }

    private Phenotype getCellAOPhenotype(String cellPhenotypeName) {
        Phenotype pheno = new Phenotype();
        pheno.setSubterm(anatomyTerm);
        pheno.setSuperterm(cellAnatomyTerm);
        GenericTerm term = new GenericTerm();
        term.setTermName(cellPhenotypeName);
        pheno.setTerm(term);
        return pheno;
    }

    private Phenotype getGOPhenotype(String goPhenotypeName) {
        Phenotype pheno = new Phenotype();
        Term goterm = new GenericTerm();
        goterm.setTermName(CILIUM);
        pheno.setSubterm(goterm);
        GenericTerm term = new GenericTerm();
        term.setTermName(goPhenotypeName);
        pheno.setTerm(term);
        pheno.setSuperterm(anatomyTerm);
        return pheno;
    }
}
