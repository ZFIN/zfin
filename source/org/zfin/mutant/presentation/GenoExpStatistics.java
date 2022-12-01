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

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        throw new RuntimeException("This method is never used so we should never see this exception");
    }
}
