package org.zfin.expression;

import org.apache.axis.encoding.ser.ArrayDeserializer;
import org.apache.log4j.Logger;
import org.zfin.expression.presentation.FigureSummaryDisplay;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * Utility methods for the Figure class
 */
public class FigureService {
    static Logger LOG = Logger.getLogger(FigureService.class);
    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();


    /**
     * Get a sorted list of genes for which expression is shown in this figure
     *
     * @param figure Figure
     * @return List of Markers
     */
    public static List<Marker> getExpressionGenes(Figure figure) {
        List<Marker> genes = new ArrayList<Marker>();
        for (ExpressionResult er : figure.getExpressionResults()) {
            ExpressionExperiment ee = er.getExpressionExperiment();
            Marker marker = ee.getGene();

            if ((marker != null)
                    && (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM))
                    && !genes.contains(marker)) {
                genes.add(ee.getGene());
            }
        }
        Collections.sort(genes);
        LOG.debug("found " + genes.size() + " genes for " + figure.getZdbID());
        return genes;
    }

    /**
     * this is the method (parameter set?) as it will be used for genotype expression display of nonstandard envs
     *
     */
    public static ExpressionSummaryCriteria createExpressionCriteria(GenotypeExperiment genox, Marker gene, boolean withImgsOnly) {
        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();
        criteria.setGenotypeExperiment(genox);
        criteria.setGene(gene);
        criteria.setWithImagesOnly(withImgsOnly);
        criteria.setStandardEnvironment(false);
        criteria.setWildtypeOnly(false);
        return criteria;
    }

    /**
     * This method (parameter set) will be used for genotype expression display of standard envs
     * @param geno genotype
     * @param gene gene
     * @param withImgsOnly require that figures joined in have images
     * @return expressionsummarycriteria object
     */
    public static ExpressionSummaryCriteria createExpressionCriteriaStandardEnvironment(Genotype geno, Marker gene, boolean withImgsOnly) {
        //assumed by the method title - we never want to do everything in the genotype *except* standard..

        boolean isStandardEnvironment = true;
        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();
        criteria.setGenotype(geno);
        criteria.setGene(gene);
        criteria.setWithImagesOnly(withImgsOnly);
        criteria.setStandardEnvironment(isStandardEnvironment);
        criteria.setWildtypeOnly(false);
        return criteria;
    }

    /**
     * This method (parameter set) will be used for genotype expression display of chemical envs
     * @param geno genotype
     * @param gene gene
     * @param withImgsOnly require that figures joined in have images
     * @return expressionsummarycriteria object
     */
    public static ExpressionSummaryCriteria createExpressionCriteriaChemicalEnvironment(Genotype geno, Marker gene, boolean withImgsOnly) {
        //assumed by title, this method is only relevant for getting the chemical environments, not for getting
        //everything *but* chemical
        boolean isChemicalEnvironment = true;

        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();
        criteria.setGenotype(geno);
        criteria.setGene(gene);
        criteria.setWithImagesOnly(withImgsOnly);
        criteria.setChemicalEnvironment(isChemicalEnvironment);
        criteria.setWildtypeOnly(false);
        return criteria;
    }


    public static List<FigureSummaryDisplay> createExpressionFigureSummary(GenotypeExperiment genox, Marker gene, boolean withImgsOnly) {
        ExpressionSummaryCriteria criteria = createExpressionCriteria(genox, gene, withImgsOnly);
        return createExpressionFigureSummary(criteria);
    }

    public static List<FigureSummaryDisplay> createExpressionFigureSummary(ExpressionSummaryCriteria expressionCriteria) {
        Set<Publication> publications = new HashSet<Publication>();
        // a map of publicationID-FigureID as keys and figure summary display objects as values
        Map<String, FigureSummaryDisplay> map = new HashMap<String, FigureSummaryDisplay>();
        List<Figure> figures;

        figures = expressionRepository.getFigures(expressionCriteria);

        for (Figure figure : figures) {

            Set<Image> imgs = figure.getImages();
            if (expressionCriteria.isWithImagesOnly() && imgs != null && imgs.isEmpty())
                continue;
            Publication pub = figure.getPublication();
            String key = pub.getZdbID() + figure.getZdbID();

            // if the key is not in the map, instantiate a display object and add it to the map
            // otherwise, get the display object from the map
            if (!map.containsKey(key)) {
                FigureSummaryDisplay figureData = new FigureSummaryDisplay();
                figureData.setPublication(pub);
                publications.add(pub);
                figureData.setFigure(figure);
                figureData.setExpressionStatementList(getFigureExpressionStatementList(figure,expressionCriteria));
                for (Image img : figure.getImages()) {
                    if (figureData.getThumbnail() == null)
                        figureData.setThumbnail(img.getThumbnail());
                }

                map.put(key, figureData);

            }
        }


        List<FigureSummaryDisplay> summaryRows = new ArrayList<FigureSummaryDisplay>();
        if (map.values().size() > 0) {
            summaryRows.addAll(map.values());
        }
        Collections.sort(summaryRows);

        return summaryRows;


    }

    public static List<ExpressionStatement> getFigureExpressionStatementList(Figure figure, ExpressionSummaryCriteria expressionCriteria) {
        //work with a clone of the original criteria, so that it doesn't get screwed up.
        ExpressionSummaryCriteria clone = expressionCriteria.clone();

        //set the figure we're limiting to
        clone.setFigure(figure);

        //unset the entities, since we want all terms that match all of the other criteria
        clone.setEntity(null);
        clone.setSingleTermEitherPosition(null);

        Set<ExpressionStatement> expressionResultSet = expressionRepository.getExpressionStatements(clone);
        List<ExpressionStatement> expressionStatementList = new ArrayList<ExpressionStatement>();

        expressionStatementList.addAll(expressionResultSet);
        return expressionStatementList;

    }

}
