package org.zfin.expression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.figure.presentation.FigureExpressionSummary;
import org.zfin.figure.repository.FigureRepository;
import org.zfin.figure.service.FigureViewService;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.figure.presentation.FigurePhenotypeSummary;
import org.zfin.mutant.*;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test against methods in the FigureService class
 */
public class FigureServiceTest extends AbstractDatabaseTest {

    static Logger logger = LogManager.getLogger(FigureServiceTest.class);

    private PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private FigureRepository figureRepository = RepositoryFactory.getFigureRepository();
    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private PhenotypeRepository phenotypeRepository = RepositoryFactory.getPhenotypeRepository();
    private FigureViewService figureViewService = new FigureViewService();

    @Test
    public void expressionGenesTest() {
        Figure figure = figureRepository.getFigure("ZDB-FIG-070307-34");
        Marker pax2a = markerRepository.getMarkerByAbbreviation("pax2a");
        Marker fgf8a = markerRepository.getMarkerByAbbreviation("fgf8a");

        List<Marker> genes = figureViewService.getExpressionGenes(figure);

        assertThat("Figure has genes", genes, notNullValue());
        assertThat("Figure contains pax2a", genes, hasItem(pax2a));
        assertThat("Figure shouldn't contains fgf8a", genes, not(hasItem(fgf8a)));

    }

    @Test
    public void getFigureExpressionSummary() {
        Figure figure = figureRepository.getFigure("ZDB-FIG-100506-2");

        FigureExpressionSummary summary = figureViewService.getFigureExpressionSummary(figure);

        assertThat("Figure has genes", summary, notNullValue());
        assertThat("Figure has 1 gene", summary.getGenes().size(), equalTo(1));
        assertThat("Figure has 1 fish", summary.getFish().size(), equalTo(1));
        assertThat("Figure has 3 antibodies", summary.getAntibodies().size(), equalTo(3));
        assertThat("Figure has 9 entities", summary.getEntities().size(), equalTo(9));
        assertThat("Figure Start", summary.getStartStage().getName(), equalTo("Segmentation:10-13 somites"));
        assertThat("Figure End", summary.getEndStage().getName(), equalTo("Hatching:Long-pec"));

        figure = figureRepository.getFigure("ZDB-FIG-050630-9812");
        summary = figureViewService.getFigureExpressionSummary(figure);

        assertThat("Figure has genes", summary, notNullValue());
        assertThat("Figure has 1 clone", summary.getProbe().getName(), equalTo("cb1"));
    }

    @Test
    public void genotypeExpressionFigureSummaryDisplayTest() {
        Figure figure = figureRepository.getFigure("ZDB-FIG-041108-3");
        FishExperiment genox = mutantRepository.getGenotypeExperiment("ZDB-GENOX-050228-2");
        Marker pax2a = markerRepository.getMarkerByID("ZDB-GENE-990415-8");

        PostComposedEntity oticPlacodeEntity = new PostComposedEntity();
        oticPlacodeEntity.setSuperterm(ontologyRepository.getTermByOboID("ZFA:0000138"));
        ExpressionStatement oticPlacodeStatement = new ExpressionStatement();
        oticPlacodeStatement.setEntity(oticPlacodeEntity);
        oticPlacodeStatement.setExpressionFound(true);


        PostComposedEntity oticVesicle = new PostComposedEntity();
        oticVesicle.setSuperterm(ontologyRepository.getTermByOboID("ZFA:0000051"));
        ExpressionStatement ectodermStatement = new ExpressionStatement();
        ectodermStatement.setEntity(oticVesicle);
        ectodermStatement.setExpressionFound(true);

        boolean withImgOnly = false;

        List<FigureSummaryDisplay> figureSummaryList = FigureService.createExpressionFigureSummary(genox, pax2a, withImgOnly);

        assertThat("figureSummaryList is not null", figureSummaryList, notNullValue());
        assertThat("figureSummaryList is not empty", figureSummaryList, not(empty()));

        List<Figure> figures = new ArrayList<>();
        Set<ExpressionStatement> statements = new HashSet<>();


        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
            statements.addAll(figureSummary.getExpressionStatementList());

            for (ExpressionStatement statement : figureSummary.getExpressionStatementList()) {
                logger.debug(figureSummary.getPublication().getShortAuthorList() + " "
                             + figureSummary.getFigure().getLabel() + " has: " + statement.getEntity().getSuperterm().getTermName());
            }

        }


        FigureSummaryDisplay fig5Summary = null;
        for (FigureSummaryDisplay fs : figureSummaryList) {
            if (fs.getFigure().equals(figure)) {
                fig5Summary = fs;
            }
        }
        assertThat("figureSummaryList should have Hans fig 5", fig5Summary, notNullValue());
        String figureLabel = fig5Summary.getPublication().getShortAuthorList() + " " + fig5Summary.getFigure().getLabel();
        assertThat(figureLabel + " should contain " + oticPlacodeStatement.getEntity().getSuperterm().getTermName(),
            fig5Summary.getExpressionStatementList(), hasItem(oticPlacodeStatement));
        //ectoderm is associaed with sox9a in this figure, not pax2a, if it comes in, the gene parameter is being ignored
        assertThat(figureLabel + " should NOT contain " + ectodermStatement.getEntity().getSuperterm().getTermName(),
            fig5Summary.getExpressionStatementList(), not(hasItem(ectodermStatement)));

    }

    @Test
    public void specificGenoxGenotypeExpressionFigureSummaryTest() {

        FishExperiment genox = mutantRepository.getGenotypeExperiment("ZDB-GENOX-050228-2");
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-990415-8");

        Figure figure = figureRepository.getFigure("ZDB-FIG-041108-3");

        // optic placode
        ExpressionStatement presentStatement = generateExpressionStatement("ZFA:0000138", null, true);
        // otic vesicle
        ExpressionStatement notPresentStatement = generateExpressionStatement("ZFA:0000051", null, true);

        boolean imagesOnly = false;

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteria(genox, gene, imagesOnly);
        genericGenotypeExpressionFigureSummaryTest(expressionCriteria, figure, presentStatement, notPresentStatement);

    }

    @Test
    @Ignore // the method, FigureService.createExpressionCriteriaStandardEnvironment,`
    // has not been used yet. ignoring it for now.
    public void standardEnvironmentOnlyGenotypeExpressionFigureSummaryTest() {

        Genotype geno = mutantRepository.getGenotypeByID("ZDB-GENO-980202-822");
        // myod1
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-980526-561");
        // Fig. 11 of Hamade et al.
        Figure figure = figureRepository.getFigure("ZDB-FIG-060201-11");

        // optic placode
        ExpressionStatement presentStatement = generateExpressionStatement("ZFA:0000138", null, false);
        ExpressionStatement notPresentStatement = generateExpressionStatement("ZFA:0007046", null, true);

        boolean imagesOnly = false;

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(geno, gene, imagesOnly);
        genericGenotypeExpressionFigureSummaryTest(expressionCriteria, figure, presentStatement, notPresentStatement);

    }

    @Test
    public void createPhenotypeFigureSummaryShouldShowFiguresSortedByPublication() {
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-980526-112");
        List<FigureSummaryDisplay> figureSummaryRows = FigureService.createPhenotypeFigureSummary(gene);
        FigureSummaryDisplay last = null;
        for (FigureSummaryDisplay row : figureSummaryRows) {
            if (last != null) {
                assertThat("publications should be sorted",
                    row.getPublication(), greaterThanOrEqualTo(last.getPublication()));
            }
            last = row;
        }
    }

    /**
     *
     */

    /**
     * The tests necessary for several cases are the same, only the data changes.
     *
     * @param expressionCriteria specific criteria for the test
     * @param figure             figure that should be present within the FigureSummaryDisplay list
     * @param presentInFigure    entity that should be returned within the figure
     * @param notPresentInFigure entity that should not be found (should be a term present
     *                           on the figure that is not present in an experiment that matches the criteria)
     * @return list of FigureSummaryDisplay objects, so that there is an option to do
     * extra tests.
     */
    public List<FigureSummaryDisplay> genericGenotypeExpressionFigureSummaryTest(ExpressionSummaryCriteria expressionCriteria,
                                                                                 Figure figure,
                                                                                 ExpressionStatement presentInFigure,
                                                                                 ExpressionStatement notPresentInFigure) {

        List<FigureSummaryDisplay> figureSummaryList = FigureService.createExpressionFigureSummary(expressionCriteria);

        assertThat("figureSummaryList is not null", figureSummaryList, notNullValue());
        assertThat("figureSummaryList is not empty", figureSummaryList, not(empty()));

        FigureSummaryDisplay figureSummaryDisplay = null;
        List<Figure> figures = new ArrayList<>();
        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
            if (figureSummary.getFigure().equals(figure)) {
                figureSummaryDisplay = figureSummary;
            }
        }

        assertThat(figure.getPublication().getShortAuthorList() + " " + figure.getLabel() + " is in the summary",
            figureSummaryDisplay, notNullValue());


        assertThat(figure.getPublication().getShortAuthorList() + " " + figure.getLabel()
                   + " has statement " + presentInFigure.getDisplayName(),
            figureSummaryDisplay.getExpressionStatementList(), hasItem(presentInFigure));

        assertThat(figure.getPublication().getShortAuthorList() + " " + figure.getLabel()
                   + " should NOT have statement " + notPresentInFigure.getDisplayName(),
            figureSummaryDisplay.getExpressionStatementList(), not(hasItem(notPresentInFigure)));

        return figureSummaryList;
    }

    @Test
    public void getPhenotypeFishFromWarehouse() {
        List<PhenotypeWarehouse> warehouseList = phenotypeRepository.getPhenotypeWarehouse("ZDB-FIG-150416-9");
        assertThat("warehouse list should not be empty", warehouseList, not(empty()));

        List<Fish> fish = figureViewService.getPhenotypeFish(warehouseList);
        assertThat("fish list should not be null", fish, notNullValue());
        assertThat("figure should have at least one fish", fish, not(empty()));
    }

    @Test
    public void getPhenotypeSTRFromWarehouse() {
        List<PhenotypeWarehouse> warehouseList = phenotypeRepository.getPhenotypeWarehouse("ZDB-FIG-150416-9");
        List<SequenceTargetingReagent> strs = figureViewService.getPhenotypeSTR(warehouseList);
        assertThat("STR list should not be null", strs, notNullValue());
    }

    @Test
    public void getPhenotypeConditionFromWarehouse() {
        List<PhenotypeWarehouse> warehouseList = phenotypeRepository.getPhenotypeWarehouse("ZDB-FIG-150416-9");
        List<Experiment> conditions = figureViewService.getPhenotypeCondition(warehouseList);
        assertThat("conditions list should not be null", conditions, notNullValue());
    }

    @Test
    public void getPhenotypeEntitiesFromWarehouse() {
        List<PhenotypeWarehouse> warehouseList = phenotypeRepository.getPhenotypeWarehouse("ZDB-FIG-150416-9");
        assertThat("warehouse list should not be empty", warehouseList, not(empty()));

        List<PostComposedEntity> entities = figureViewService.getPhenotypeEntitiesFromWarehouse(warehouseList);
        assertThat("entities should not be null", entities, notNullValue());
        assertThat("figure should have at least one entity", entities, not(empty()));
    }

    @Test
    public void getPhenotypeStagesFromWarehouse() {
        List<PhenotypeWarehouse> warehouseList = phenotypeRepository.getPhenotypeWarehouse("ZDB-FIG-150416-9");
        assertThat("warehouse list should not be empty", warehouseList, not(empty()));

        DevelopmentStage startStage = figureViewService.getPhenotypeStartStage(warehouseList);
        DevelopmentStage endStage = figureViewService.getPhenotypeEndStage(warehouseList);
        assertThat("start stage should not be null", startStage, notNullValue());
        assertThat("end stage should not be null", endStage, notNullValue());
        assertThat("start stage should be <= end stage", startStage, lessThanOrEqualTo(endStage));
    }

    @Test
    public void getPhenotypeStagesFromEmptyWarehouse() {
        List<PhenotypeWarehouse> emptyList = new ArrayList<>();
        DevelopmentStage startStage = figureViewService.getPhenotypeStartStage(emptyList);
        DevelopmentStage endStage = figureViewService.getPhenotypeEndStage(emptyList);
        assertThat("start stage should be null for empty list", startStage, nullValue());
        assertThat("end stage should be null for empty list", endStage, nullValue());
    }

    private ExpressionStatement generateExpressionStatement(String superTermOboID, String subTermOboID, boolean isExpressionFound) {
        ExpressionStatement statement = new ExpressionStatement();
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(ontologyRepository.getTermByOboID(superTermOboID));
        if (subTermOboID != null) {
            entity.setSubterm(ontologyRepository.getTermByOboID(subTermOboID));
        }
        statement.setEntity(entity);
        statement.setExpressionFound(isExpressionFound);

        return statement;
    }

}
