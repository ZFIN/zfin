package org.zfin.antibody;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.ontology.GoTerm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setAnatomyTerm(termTwo);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setAnatomyTerm(termThree);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setAnatomyTerm(termThree);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);
        results.add(resultFour);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        List<AnatomyItem> aoTerms = as.getDistinctAnatomyTerms();
        assertTrue(aoTerms != null);

    }

    @Test
    public void singleGoTermList(){
        GoTerm termOne = new GoTerm();
        termOne.setZdbID("ZDB-GOTERM-030325-149");
        termOne.setName("nucleus");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setGoTerm(termOne);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

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

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

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

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setGoTerm(termTwo);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

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

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setGoTerm(termTwo);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setGoTerm(termOne);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

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

        ExpressionResult resultTwo = new ExpressionResult();

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setGoTerm(termOne);

        HashSet<ExpressionResult> results = new HashSet<ExpressionResult>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        Set<ExpressionExperiment> experiments = new HashSet<ExpressionExperiment>();
        experiments.add(experiment);

        Antibody ab = new Antibody();
        ab.setAntibodyLabelings(experiments);

        AntibodyService as = new AntibodyService(ab);

        Set<GoTerm> goTerms = as.getDistinctGoTermsWTAndStandard();
        assertTrue(goTerms != null);
        assertEquals(1, goTerms.size());
    }
}