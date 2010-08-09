package org.zfin.antibody;

import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.marker.MarkerAlias;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests AntibodyService class.
 */
public class AntibodyServiceTest {

    @Test
    public void distinctAOTermList(){
        AnatomyItem termOne = new AnatomyItem();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        termOne.setNameOrder("Halle");
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        termTwo.setNameOrder("Zwitter");
        AnatomyItem termThree = new AnatomyItem();
        termThree.setZdbID("ZDB-ANAT-010921-572");
        termThree.setNameOrder("Engel");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperterm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSuperterm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSuperterm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setSuperterm(termThree);
        resultFour.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);
        results.add(resultFour);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> aoTerms = as.getDistinctAoTerms();
        assertTrue(aoTerms != null);
        assertEquals(3, aoTerms.size());
    }

    @Test
    public void distinctAOTermListWithDuplicate(){
        AnatomyItem termOne = new AnatomyItem();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        termOne.setNameOrder("Halle");
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        termTwo.setNameOrder("Zwitter");
        AnatomyItem termThree = new AnatomyItem();
        termThree.setZdbID("ZDB-ANAT-011113-512");
        termThree.setNameOrder("Halle");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperterm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSuperterm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSuperterm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setSuperterm(termThree);
        resultFour.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);
        results.add(resultFour);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);

        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> aoTerms = as.getDistinctAoTerms();
        assertTrue(aoTerms != null);
        assertEquals(2, aoTerms.size());
    }

    @Test
    public void distinctAOTermListWithSecondaryAoTerm(){
        AnatomyItem termOne = new AnatomyItem();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        termOne.setNameOrder("Halle");
        AnatomyItem termTwo = new AnatomyItem();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        termTwo.setNameOrder("Zwitter");
        AnatomyItem termThree = new AnatomyItem();
        termThree.setZdbID("ZDB-ANAT-011113-514");
        termThree.setNameOrder("Margor");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperterm(termOne);
        resultOne.setExpressionFound(true);
        resultOne.setSubterm(termThree);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSuperterm(termTwo);
        resultTwo.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);

        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> aoTerms = as.getDistinctAoTerms();
        assertTrue(aoTerms != null);
        assertEquals(3, aoTerms.size());
    }

    private Set<ExpressionExperiment> createWildtypeAndStandardGenotypeExperiment(ExpressionExperiment experiment) {
        GenotypeExperiment genox = new GenotypeExperiment();
        experiment.setGenotypeExperiment(genox);
        Genotype geno = new Genotype();
        geno.setWildtype(true);
        genox.setGenotype(geno);
        Experiment exp = new Experiment();
        exp.setName(Experiment.STANDARD);
        genox.setExperiment(exp);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);
        return experiments;
    }

    @Test
    public void singleGoTermList(){
        Term termOne = getNucleusTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubterm(termOne);
        resultOne.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(1, goTerms.size());
    }

    @Test
    public void noGoTermList(){

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(0, goTerms.size());
    }

    @Test
    public void multipleDistinctGoTermList(){
        Term termOne = getNucleusTerm();

        Term termTwo = getCyokineTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubterm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSubterm(termTwo);
        resultTwo.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);

        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(2, goTerms.size());
    }

    private Term getCyokineTerm() {
        Term termTwo = new GenericTerm();
        termTwo.setID("ZDB-TERM-091209-3709");
        termTwo.setTermName("cytokine activity");
        termTwo.setOntology(Ontology.GO_MF);
        return termTwo;
    }

    private Term getNucleusTerm() {
        Term termOne = new GenericTerm();
        termOne.setID("ZDB-TERM-091209-4086");
        termOne.setTermName("nucleus");
        termOne.setOntology(Ontology.GO_CC);
        return termOne;
    }

    @Test
    public void multipleRepeatingGoTermList(){
        Term termOne = getNucleusTerm();
        Term termTwo = getCyokineTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubterm(termOne);
        resultOne.setExpressionFound(true);
        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSubterm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSubterm(termOne);
        resultThree.setExpressionFound(true);
        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(2, goTerms.size());
    }

    @Test
    public void multipleRepeatingGoTermListWithNull(){
        Term termOne = getNucleusTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubterm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSubterm(termOne);
        resultThree.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        createWildtypeAndStandardGenotypeExperiment(experiment);

        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<Term> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(1, goTerms.size());
    }

    /**
     * Check that an antibody is found by an alias name, and case-insensitive
     * and correctly matched
     */
    @Test
    public void matchingTextForAntibodyByAlias() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String abName = "veg";
        searchCriteria.setName(abName);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);

        Antibody antibody = new Antibody();
        antibody.setName("anti-Tbx16");
        MarkerAlias alias = new MarkerAlias();
        alias.setAlias("anti-VegT");
        HashSet<MarkerAlias> aliases = new HashSet<MarkerAlias>();
        aliases.add(alias);
        antibody.setAliases(aliases);

        AntibodyService service = new AntibodyService(antibody);
        service.setAntibodySearchCriteria(searchCriteria);

        List<MatchingText> matchingTexts = new ArrayList<MatchingText>();
        service.addMatchingAntibodyName(matchingTexts);
        assertTrue(matchingTexts.size() > 0);
    }

    /**
     * Check that an antibody is found by an alias name, and case-insensitive
     * and correctly matched
     */
    @Test
    public void matchingTextForAntibodyByAliasTwo() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String abName = "   i-V ";
        searchCriteria.setName(abName);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);

        Antibody antibody = new Antibody();
        antibody.setName("anti-Tbx16");
        MarkerAlias alias = new MarkerAlias();
        alias.setAlias("anti-VegT");
        HashSet<MarkerAlias> aliases = new HashSet<MarkerAlias>();
        aliases.add(alias);
        antibody.setAliases(aliases);

        AntibodyService service = new AntibodyService(antibody);
        service.setAntibodySearchCriteria(searchCriteria);

        List<MatchingText> matchingTexts = new ArrayList<MatchingText>();
        service.addMatchingAntibodyName(matchingTexts);
        assertTrue(matchingTexts.size() > 0);
    }


}