package org.zfin.expression;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test against methods in the FigureService class
 */
public class FigureServiceTest extends AbstractDatabaseTest {

    static Logger logger = Logger.getLogger(FigureServiceTest.class);


    ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();
    PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    


    @Test
    public void expressionGenesTest() {
        Figure figure = publicationRepository.getFigure("ZDB-FIG-070307-34");
        Marker pax2a = markerRepository.getMarkerByAbbreviation("pax2a");
        Marker fgf8a = markerRepository.getMarkerByAbbreviation("fgf8a");
        
        List<Marker> genes = FigureService.getExpressionGenes(figure);

        assertNotNull("Figure has genes", genes);
        assertTrue("Figure contains pax2a", genes.contains(pax2a));
        assertFalse("Figure shouldn't contains fgf8a", genes.contains(fgf8a));
        
    }

    @Test
    public void genotypeExpressionFigureSummaryDisplayTest() {
        Figure figure = publicationRepository.getFigure("ZDB-FIG-041108-3");
        GenotypeExperiment genox = mutantRepository.getGenotypeExperiment("ZDB-GENOX-050228-2");
        Marker pax2a = markerRepository.getMarkerByID("ZDB-GENE-990415-8");

        PostComposedEntity oticPlacodeEntity = new PostComposedEntity();
        oticPlacodeEntity.setSuperterm(ontologyRepository.getTermByOboID("ZFA:0000138"));
        ExpressionStatement oticPlacodeStatement = new ExpressionStatement();
        oticPlacodeStatement.setEntity(oticPlacodeEntity);
        oticPlacodeStatement.setExpressionFound(true);


        PostComposedEntity ectodermEntity = new PostComposedEntity();
        ectodermEntity.setSuperterm(ontologyRepository.getTermByOboID("ZFA:0000016"));
        ExpressionStatement ectodermStatement = new ExpressionStatement();
        ectodermStatement.setEntity(ectodermEntity);
        ectodermStatement.setExpressionFound(true);

        boolean withImgOnly = false;

        List<FigureSummaryDisplay> figureSummaryList = FigureService.createExpressionFigureSummary(genox, pax2a, withImgOnly);

        assertNotNull("figureSummaryList is not null", figureSummaryList);
        assertTrue("figureSummaryList is not empty", figureSummaryList.size() > 0);

        List<Figure> figures = new ArrayList<Figure>();
        Set<ExpressionStatement> statements = new HashSet<ExpressionStatement>();


        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
            statements.addAll(figureSummary.getExpressionStatementList());

            for (ExpressionStatement statement: figureSummary.getExpressionStatementList()) {
                logger.debug(figureSummary.getPublication().getShortAuthorList() + " "
                        + figureSummary.getFigure().getLabel() + " has: " + statement.getEntity().getSuperterm().getTermName());
            }

        }




        FigureSummaryDisplay fig5Summary = null;
        for (FigureSummaryDisplay fs : figureSummaryList) {
            if (fs.getFigure().equals(figure))
                fig5Summary = fs;
        }
        assertNotNull("figureSummaryList should have Hans fig 5", fig5Summary);
        assertTrue(fig5Summary.getPublication().getShortAuthorList() + " "
                + fig5Summary.getFigure().getLabel() + " should contain " + oticPlacodeStatement.getEntity().getSuperterm().getTermName(), fig5Summary.getExpressionStatementList().contains(oticPlacodeStatement));
        //ectoderm is associaed with sox9a in this figure, not pax2a, if it comes in, the gene parameter is being ignored
        assertFalse(fig5Summary.getPublication().getShortAuthorList()
                + " " + fig5Summary.getFigure().getLabel() + " should NOT contain " + ectodermStatement.getEntity().getSuperterm().getTermName(), fig5Summary.getExpressionStatementList().contains(ectodermStatement));

    }

    @Test void specificGenoxGenotypeExpressionFigureSummaryTest() {

        GenotypeExperiment genox = mutantRepository.getGenotypeExperiment("ZDB-GENOX-050228-2");
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-990415-8");

        Figure figure = publicationRepository.getFigure("ZDB-FIG-041108-3");

        ExpressionStatement presentStatement = generateExpressionStatement("ZFA:0000138",null,true);
        ExpressionStatement notPresentStatement = generateExpressionStatement("ZFA:0000016",null,true);

        boolean imagesOnly = false;

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteria(genox, gene, imagesOnly);
        genericGenotypeExpressionFigureSummaryTest(expressionCriteria, figure, presentStatement, notPresentStatement);

    }

    @Test
    public void chemicalOnlyGenotypeExpressionFigureSummaryTest() {

        Genotype geno = mutantRepository.getGenotypeByID("ZDB-GENO-041129-4");
        Marker gene = markerRepository.getMarkerByID("ZDB-EFG-070117-1");

        Figure figure = publicationRepository.getFigure("ZDB-FIG-081107-10");

        ExpressionStatement presentStatement = generateExpressionStatement("ZFA:0009080",null,true);
        ExpressionStatement notPresentStatement = generateExpressionStatement("ZFA:0009292",null,true);

        boolean imagesOnly = false;

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaChemicalEnvironment(geno, gene, imagesOnly);
        genericGenotypeExpressionFigureSummaryTest(expressionCriteria, figure, presentStatement, notPresentStatement);


    }

    @Test
    public void standardEnvironmentOnlyGenotypeExprsesionFigureSummaryTest() {

        Genotype geno = mutantRepository.getGenotypeByID("ZDB-GENO-980202-822");
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-980526-561");

        Figure figure = publicationRepository.getFigure("ZDB-FIG-060201-11");

        ExpressionStatement presentStatement = generateExpressionStatement("ZFA:0000138",null,false);
        ExpressionStatement notPresentStatement = generateExpressionStatement("ZFA:0007046",null,true);

        boolean imagesOnly = false;

        ExpressionSummaryCriteria expressionCriteria = FigureService.createExpressionCriteriaStandardEnvironment(geno, gene, imagesOnly);
        genericGenotypeExpressionFigureSummaryTest(expressionCriteria, figure, presentStatement, notPresentStatement);

    }

    /**
     *
     */

    /**
     * The tests necessary for several cases are the same, only the data changes.
     * @param expressionCriteria specific criteria for the test
     * @param figure figure that should be present within the FigureSummaryDisplay list
     * @param presentInFigure entity that should be returned within the figure
     * @param notPresentInFigure entity that should not be found (should be a term present
     * on the figure that is not present in an experiment that matches the criteria)
     * @return list of FigureSummaryDisplay objects, so that there is an option to do
     *  extra tests.
     */
    public List<FigureSummaryDisplay> genericGenotypeExpressionFigureSummaryTest(ExpressionSummaryCriteria expressionCriteria,
                                                           Figure figure,
                                                           ExpressionStatement presentInFigure,
                                                           ExpressionStatement notPresentInFigure) {
        
        List<FigureSummaryDisplay> figureSummaryList = FigureService.createExpressionFigureSummary(expressionCriteria);

        assertNotNull("figureSummaryList is not null", figureSummaryList);
        assertTrue("figureSummaryList is not empty", figureSummaryList.size() > 0);

        FigureSummaryDisplay figureSummaryDisplay = null;
        List<Figure> figures = new ArrayList<Figure>();
        for (FigureSummaryDisplay figureSummary : figureSummaryList) {
            figures.add(figureSummary.getFigure());
            if (figureSummary.getFigure().equals(figure)) {
                figureSummaryDisplay = figureSummary;
            }
        }

        assertTrue(figure.getPublication().getShortAuthorList() + " " + figure.getLabel() + " is in the summary",
                   figureSummaryDisplay != null);


        assertTrue(figure.getPublication().getShortAuthorList() + " " + figure.getLabel()
                + " has statement " + presentInFigure.getDisplayName(),
                figureSummaryDisplay.getExpressionStatementList().contains(presentInFigure));

        assertTrue(figure.getPublication().getShortAuthorList() + " " + figure.getLabel()
                + " should NOT have statement " + notPresentInFigure.getDisplayName(),
                !figureSummaryDisplay.getExpressionStatementList().contains(notPresentInFigure));

        return figureSummaryList;
    }

    private ExpressionStatement generateExpressionStatement(String superTermOboID, String subTermOboID, boolean isExpressionFound ) {
        ExpressionStatement statement = new ExpressionStatement();
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(ontologyRepository.getTermByOboID(superTermOboID));
        if (subTermOboID != null)
            entity.setSubterm(ontologyRepository.getTermByOboID(subTermOboID));
        statement.setEntity(entity);
        statement.setExpressionFound(isExpressionFound);

        return statement;
    }

}
