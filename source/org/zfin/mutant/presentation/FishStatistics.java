package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.repository.FishService;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenient class to show statistics about phenotypes related to a given AO term..
 */
public class FishStatistics extends EntityStatistics {

    private Fish fish;
    private GenericTerm anatomyItem;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet
    private Set<Publication> publicationSet = null; // null indicates that this has not been populated yet
    private boolean includeSubstructures;

    public FishStatistics(Fish fish) {
        this.fish = fish;
    }

    public FishStatistics(Fish fish, GenericTerm anatomyItem) {
        this.fish = fish;
        this.anatomyItem = anatomyItem;
    }

    public FishStatistics(Fish fish, GenericTerm anatomyItem, boolean includeSubstructures) {
        this.fish = fish;
        this.anatomyItem = anatomyItem;
        this.includeSubstructures = includeSubstructures;
    }

    public Fish getFish() {
        return fish;
    }

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        return null;
    }

    public int getNumberOfFigures() {
        if (figureResults == null) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByFishAndAnatomy(fish, anatomyItem, includeSubstructures);
            if (publicationSet == null) {
                publicationSet = new TreeSet<>();
                for (Figure figure : figureResults.getPopulatedResults()) {
                    publicationSet.add(figure.getPublication());
                }
            }
        }
        return figureResults.getTotalCount();
    }

    public boolean isImgInFigure() {
        if (figureResults == null || figureResults.getTotalCount() == 0) {
            return false;
        }
        boolean thereIsImg = false;
        for (Figure fig : figureResults.getPopulatedResults()) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }

    /**
     * @return There should be a single figure per GenotypeStatistics
     */
    public Figure getFigure() {
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByFishAndAnatomy(fish, anatomyItem);
        }
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        }
        return figureResults.getPopulatedResults().get(0);
    }

    public Set<Publication> getPublicationSet() {
        return publicationSet;
    }

    public List<Marker> getAffectedMarkers() {
        return FishService.getAffectedGenes(fish);
    }


    public Set<PhenotypeStatement> getPhenotypeStatements() {
        Set<PhenotypeStatement> phenotypeStatements = new TreeSet<>();
        phenotypeStatements.addAll(PhenotypeService.getPhenotypeStatements(fish, anatomyItem, includeSubstructures));
        return phenotypeStatements;
    }

    public Set<PhenotypeStatementWarehouse> getPhenotypeObserved() {
        Set<PhenotypeStatementWarehouse> phenotypeObserved = new TreeSet<>();
        phenotypeObserved.addAll(PhenotypeService.getPhenotypeObserved(fish, anatomyItem, includeSubstructures));
        return phenotypeObserved;
    }


}
