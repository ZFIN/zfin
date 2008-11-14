package org.zfin.antibody;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Experiment;
import org.zfin.ontology.GoTerm;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.util.FilterType;
import org.zfin.framework.presentation.MatchingText;
import org.zfin.marker.MarkerAlias;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Genotype;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

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
        resultOne.setAnatomyTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setAnatomyTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setAnatomyTerm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setAnatomyTerm(termThree);
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

        List<AnatomyItem> aoTerms = as.getDistinctAnatomyTerms();
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
        resultOne.setAnatomyTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setAnatomyTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setAnatomyTerm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setAnatomyTerm(termThree);
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

        List<AnatomyItem> aoTerms = as.getDistinctAnatomyTerms();
        assertTrue(aoTerms != null);
        assertEquals(2, aoTerms.size());
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
        GoTerm termOne = new GoTerm();
        termOne.setZdbID("ZDB-GOTERM-030325-149");
        termOne.setName("nucleus");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setGoTerm(termOne);
        resultOne.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = createWildtypeAndStandardGenotypeExperiment(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
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

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(0, goTerms.size());
    }

    @Test
    public void multipleDistinctGoTermList(){
        GoTerm termOne = new GoTerm();
        termOne.setZdbID("ZDB-GOTERM-030325-149");
        termOne.setName("nucleus");

        GoTerm termTwo = new GoTerm();
        termTwo.setZdbID("ZDB-GOTERM-030325-108");
        termTwo.setName("cytokine activity");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setGoTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setGoTerm(termTwo);
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

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(2, goTerms.size());
    }

    @Test
    public void multipleRepeatingGoTermList(){
        GoTerm termOne = new GoTerm();
        termOne.setZdbID("ZDB-GOTERM-030325-149");
        termOne.setName("nucleus");

        GoTerm termTwo = new GoTerm();
        termTwo.setZdbID("ZDB-GOTERM-030325-108");
        termTwo.setName("cytokine activity");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setGoTerm(termOne);
        resultOne.setExpressionFound(true);
        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setGoTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setGoTerm(termOne);
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

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(2, goTerms.size());
    }

    @Test
    public void multipleRepeatingGoTermListWithNull(){
        GoTerm termOne = new GoTerm();
        termOne.setZdbID("ZDB-GOTERM-030325-149");
        termOne.setName("nucleus");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setGoTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setGoTerm(termOne);
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

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
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
        service.setAntibodySerachCriteria(searchCriteria);

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
        service.setAntibodySerachCriteria(searchCriteria);

        List<MatchingText> matchingTexts = new ArrayList<MatchingText>();
        service.addMatchingAntibodyName(matchingTexts);
        assertTrue(matchingTexts.size() > 0);
    }


}