package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.Genotype;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Prita
 * Date: Aug 5, 2009
 * Time: 3:21:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenoExpStatistics extends EntityStatistics {

    private Genotype genotype;
    private Feature feature;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet

    public GenoExpStatistics(Genotype genotype, Feature feature) {
        this.genotype = genotype;
        this.feature = feature;
    }

    public Genotype getGenotype() {
        return genotype;
    }


    public int getNumberOfFigures() {
        if (figureResults == null) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoExp(genotype);
        }
        return figureResults.getTotalCount();
    }

    /**
     * @return There should be a single figure per GenotypeStatistics
     */
    public Figure getFigure() {
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoExp(genotype);
        }
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        }
        return figureResults.getPopulatedResults().get(0);
    }

    protected PaginationResult<Publication> getPublicationPaginationResult() {
        PublicationRepository repository = RepositoryFactory.getPublicationRepository();
        return repository.getPublicationsWithFiguresbyGenoExp(genotype);
    }


    /*public Map<String, Set> getPhenotypeDescriptions() {
   Map<String, Set> phenotypes = new HashMap<String, Set>();
   Set<GenotypeExperiment> genotypeExperiments = genotype.getGenotypeExperiments();
   for (GenotypeExperiment genoExperiment : genotypeExperiments) {
       phenotypes.putAll(PhenotypeService.getPhenotypesGroupedByOntology(genoExperiment, anatomyItem));
   }
   return phenotypes;*/
}
