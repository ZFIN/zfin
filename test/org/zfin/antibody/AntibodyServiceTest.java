package org.zfin.antibody;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.*;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.infrastructure.DataAliasGroup;
import org.zfin.marker.MarkerAlias;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getAnatomyRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

/**
 * Tests AntibodyService class.
 */
public class AntibodyServiceTest extends AbstractDatabaseTest {

    private static final Logger logger = Logger.getLogger(AntibodyServiceTest.class);


    @Test
    public void distinctAOTermList() {
        GenericTerm termOne = new GenericTerm();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        termOne.setTermName("Halle");
        GenericTerm termTwo = new GenericTerm();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        termTwo.setTermName("Zwitter");
        GenericTerm termThree = new GenericTerm();
        termThree.setZdbID("ZDB-ANAT-010921-572");
        termThree.setTermName("Engel");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSuperTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSuperTerm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setSuperTerm(termThree);
        resultFour.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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
    public void distinctAOTermListWithDuplicate() {
        GenericTerm termOne = new GenericTerm();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        termOne.setTermName("Halle");
        GenericTerm termTwo = new GenericTerm();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        termTwo.setTermName("Zwitter");
        GenericTerm termThree = new GenericTerm();
        termThree.setZdbID("ZDB-ANAT-011113-512");
        termThree.setTermName("Halle");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSuperTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSuperTerm(termThree);
        resultThree.setExpressionFound(true);

        ExpressionResult resultFour = new ExpressionResult();
        resultFour.setSuperTerm(termThree);
        resultFour.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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
    public void distinctAOTermListWithSecondaryAoTerm() {
        GenericTerm termOne = new GenericTerm();
        termOne.setZdbID("ZDB-ANAT-011113-512");
        GenericTerm termTwo = new GenericTerm();
        termTwo.setZdbID("ZDB-ANAT-011113-223");
        GenericTerm termThree = new GenericTerm();
        termThree.setZdbID("ZDB-ANAT-011113-514");

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSuperTerm(createMinimalGenericTerm(termOne, Ontology.ANATOMY));
        resultOne.setExpressionFound(true);
        resultOne.setSubTerm(createMinimalGenericTerm(termThree, Ontology.ANATOMY));

        ExpressionResult resultTwo = new ExpressionResult();
        resultOne.setSuperTerm(createMinimalGenericTerm(termTwo, Ontology.ANATOMY));
        resultTwo.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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
        FishExperiment genox = new FishExperiment();
        genox.setStandard(true);
        genox.setStandardOrGenericControl(true);
        experiment.setFishExperiment(genox);
        Fish fish = new Fish();
        Genotype geno = new Genotype();
        geno.setWildtype(true);
        fish.setGenotype(geno);
        genox.setFish(fish);
        Experiment exp = new Experiment();
        genox.setExperiment(exp);
        Set<ExpressionExperiment> experiments = new HashSet<>();
        experiments.add(experiment);
        return experiments;
    }

    @Test
    public void singleGoTermList() {
        GenericTerm termOne = getNucleusTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubTerm(termOne);
        resultOne.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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
    public void noGoTermList() {

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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
    public void multipleDistinctGoTermList() {
        GenericTerm termOne = getNucleusTerm();

        GenericTerm termTwo = getCyokineTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSubTerm(termTwo);
        resultTwo.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
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

    private GenericTerm getCyokineTerm() {
        GenericTerm termTwo = new GenericTerm();
        termTwo.setZdbID("ZDB-TERM-091209-3709");
        termTwo.setTermName("cytokine activity");
        termTwo.setOntology(Ontology.GO_MF);
        return termTwo;
    }

    private GenericTerm getNucleusTerm() {
        GenericTerm termOne = new GenericTerm();
        termOne.setZdbID("ZDB-TERM-091209-4086");
        termOne.setTermName("nucleus");
        termOne.setOntology(Ontology.GO_CC);
        return termOne;
    }

    @Test
    public void multipleRepeatingGoTermList() {
        GenericTerm termOne = getNucleusTerm();
        GenericTerm termTwo = getCyokineTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubTerm(termOne);
        resultOne.setExpressionFound(true);
        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setSubTerm(termTwo);
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSubTerm(termOne);
        resultThree.setExpressionFound(true);
        HashSet<ExpressionResult> results = new HashSet<>();
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
    public void multipleRepeatingGoTermListWithNull() {
        GenericTerm termOne = getNucleusTerm();

        ExpressionResult resultOne = new ExpressionResult();
        resultOne.setSubTerm(termOne);
        resultOne.setExpressionFound(true);

        ExpressionResult resultTwo = new ExpressionResult();
        resultTwo.setExpressionFound(true);

        ExpressionResult resultThree = new ExpressionResult();
        resultThree.setSubTerm(termOne);
        resultThree.setExpressionFound(true);

        HashSet<ExpressionResult> results = new HashSet<>();
        results.add(resultOne);
        results.add(resultTwo);
        results.add(resultThree);

        ExpressionExperiment experiment = new ExpressionExperiment();
        experiment.setExpressionResults(results);
        createWildtypeAndStandardGenotypeExperiment(experiment);

        Set<ExpressionExperiment> experiments = new HashSet<>();
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
        alias.setDataZdbID("ZDB-ATB-090802-2");
        DataAliasGroup group = new DataAliasGroup();
        group.setName(DataAliasGroup.Group.ALIAS.name());
        alias.setAliasGroup(group);
        HashSet<MarkerAlias> aliases = new HashSet<>();
        aliases.add(alias);
        antibody.setAliases(aliases);

        AntibodyService service = new AntibodyService(antibody);
        service.setAntibodySearchCriteria(searchCriteria);

        assertTrue(service.getMatchingText().size() > 0);
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
        alias.setDataZdbID("ZDB-ATB-090802-2");
        DataAliasGroup group = new DataAliasGroup();
        group.setName(DataAliasGroup.Group.ALIAS.name());
        alias.setAliasGroup(group);
        HashSet<MarkerAlias> aliases = new HashSet<>();
        aliases.add(alias);
        antibody.setAliases(aliases);

        AntibodyService service = new AntibodyService(antibody);
        service.setAntibodySearchCriteria(searchCriteria);

        assertTrue(service.getMatchingText().size() > 0);
    }


    /**
     * From the anatomy page, the call passes in a single term and an antibody
     */
    @Test
    public void figureSummaryTestFromAnatomyPage() {
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID("ZDB-ATB-081002-10");
        assertNotNull(antibody);

        GenericTerm neuron = getOntologyRepository().getTermByOboID("ZFA:0009248");
        assertNotNull(neuron);

        DevelopmentStage start = getAnatomyRepository().getStageByID("ZDB-STAGE-010723-30");
        DevelopmentStage end = getAnatomyRepository().getStageByID("ZDB-STAGE-010723-39");

        Figure fig2 = RepositoryFactory.getPublicationRepository().getFigure("ZDB-FIG-081002-2");
        AntibodyService antibodyService = new AntibodyService(antibody);
        ExpressionSummaryCriteria criteria = antibodyService.createExpressionSummaryCriteria(neuron, null, start, end, false);
        antibodyService.createFigureSummary(criteria);

        List<FigureSummaryDisplay> figureSummaryList = antibodyService.getFigureSummary();
        List<Figure> figures = new ArrayList<>();


        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
        }

        assertTrue("Figure Summary should contain fig2", figures.contains(fig2));

    }


    /**
     * from the antibody page, it's a summary for a single antibody, a single stage and
     */
    @Test
    public void figureSummaryTestFromAntibodyPage() {
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID("ZDB-ATB-081003-1");
        assertNotNull(antibody);

        GenericTerm slowMuscleCell = getOntologyRepository().getTermByOboID("ZFA:0009116");
        assertNotNull(slowMuscleCell);
        PostComposedEntity slowMuscleCellEntity = new PostComposedEntity();
        slowMuscleCellEntity.setSuperterm(slowMuscleCell);
        ExpressionStatement slowMuscleCellStatement = new ExpressionStatement();
        slowMuscleCellStatement.setEntity(slowMuscleCellEntity);
        slowMuscleCellStatement.setExpressionFound(true);


        GenericTerm myotome = getOntologyRepository().getTermByOboID("ZFA:0001056");
        assertNotNull(myotome);
        PostComposedEntity myotomeEntity = new PostComposedEntity();
        myotomeEntity.setSuperterm(myotome);
        ExpressionStatement myotomeStatement = new ExpressionStatement();
        myotomeStatement.setEntity(myotomeEntity);
        myotomeStatement.setExpressionFound(true);


        DevelopmentStage prim5 = RepositoryFactory.getAnatomyRepository().getStageByID("ZDB-STAGE-010723-10");
        assertNotNull(prim5);

        Figure figS3 = RepositoryFactory.getPublicationRepository().getFigure("ZDB-FIG-081117-50");

        AntibodyService antibodyService = new AntibodyService(antibody);

        ExpressionSummaryCriteria criteria = antibodyService.createExpressionSummaryCriteria(slowMuscleCell, null, prim5, prim5, false);
        antibodyService.createFigureSummary(criteria);

        List<FigureSummaryDisplay> figureSummaryList = antibodyService.getFigureSummary();
        List<Figure> figures = new ArrayList<>();
        Set<ExpressionStatement> statements = new HashSet<>();


        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
            statements.addAll(figureSummary.getExpressionStatementList());

            //all figures returned should have slow muscle cell
            assertTrue(figureSummary.getPublication().getShortAuthorList() + " " + figureSummary.getFigure().getLabel()
                    + " should have " + slowMuscleCellStatement.getEntity().getSuperterm().getTermName(), figureSummary.getExpressionStatementList().contains(slowMuscleCellStatement));
        }


        //find the figure 6 summary, test against it
        FigureSummaryDisplay figS3Summary = null;
        for (FigureSummaryDisplay fs : figureSummaryList) {
            if (fs.getFigure().equals(figS3)) {
                figS3Summary = fs;
            }
        }
        assertNotNull("figureSummaryList should have " + figS3.getPublication().getShortAuthorList() + " " + figS3.getLabel(), figS3Summary);
        assertTrue(figS3.getPublication().getShortAuthorList() + " " + figS3.getLabel() + "expression statement list should contain myotome", figS3Summary.getExpressionStatementList().contains(myotomeStatement));


    }

    @Test
    public void distinctLabelingStatements() {
        // zna-1
        // has AO : CC combinations.
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID("ZDB-ATB-081002-24");
        AntibodyService service = new AntibodyService(antibody);
        List<ExpressionStatement> labeledTerms = service.getAntibodyLabelingStatements();
        assertNotNull(labeledTerms);
    }

    public GenericTerm createMinimalGenericTerm(GenericTerm genericTerm, Ontology ontology) {
        genericTerm.setOntology(ontology);
        return genericTerm;
    }

}