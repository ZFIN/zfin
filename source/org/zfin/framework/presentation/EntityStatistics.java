package org.zfin.framework.presentation;

import org.zfin.publication.Publication;
import org.zfin.expression.Figure;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 *
 */
public abstract class EntityStatistics {

    protected List<Publication> publications;
    protected int numberOfPublications = -1;

    public int getNumberOfPublications() {
        if (numberOfPublications == -1) {
            PaginationResult<Publication> pubs = getPublicationPaginationResult();
            if (pubs == null) {
                numberOfPublications = 0;
            } else {
                numberOfPublications = pubs.getTotalCount();
                if (numberOfPublications == 1)
                    publications = pubs.getPopulatedResults();
            }
        }
        return numberOfPublications;
    }

    /**
     * Override this method to retrieve the publication for a given entity statistics.
     * @return pagination result.
     */
    protected abstract PaginationResult<Publication> getPublicationPaginationResult();

    public Publication getSinglePublication() {
        if (publications == null) {
            // make sure the query is run.
            getNumberOfPublications();
        }
        if (publications == null || publications.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one publication");
        return publications.iterator().next();
    }

    public abstract Figure getFigure();

    public abstract int getNumberOfFigures();
}
