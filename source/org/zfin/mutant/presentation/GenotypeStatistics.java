package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerTypeGroup;
import org.zfin.mutant.*;
import org.zfin.repository.RepositoryFactory;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.util.*;

/**
 * ToDo: Please add documentation for this class.
 */
public class GenotypeStatistics extends EntityStatistics {

    private Genotype genotype;
    private AnatomyItem anatomyItem;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet

    public GenotypeStatistics(Genotype genotype, AnatomyItem anatomyItem) {
        this.genotype = genotype;
        this.anatomyItem = anatomyItem;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public int getNumberOfFigures() {
        if (figureResults == null) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoAndAnatomy(genotype, anatomyItem);
        }
        return figureResults.getTotalCount();
    }

    /**
     * @return There should be a single figure per GenotypeStatistics
     */
    public Figure getFigure() {
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            figureResults = RepositoryFactory.getPublicationRepository().getFiguresByGenoAndAnatomy(genotype, anatomyItem);
        }
        if (figureResults == null || figureResults.getTotalCount() != 1) {
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        }
        return figureResults.getPopulatedResults().get(0);
    }

    protected PaginationResult<Publication> getPublicationPaginationResult() {
        PublicationRepository repository = RepositoryFactory.getPublicationRepository();
        return repository.getPublicationsWithFigures(genotype, anatomyItem);
    }

    public List<Marker> getAffectedMarkers() {
        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
        List<Marker> markers = new ArrayList<Marker>();
        for (GenotypeFeature feat : features) {
            Feature feature = feat.getFeature();
            Set<FeatureMarkerRelationship> rels = feature.getFeatureMarkerRelations();
            for (FeatureMarkerRelationship rel : rels) {
                Marker marker = rel.getMarker();
                // Only add true genes
                if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    markers.add(marker);
                }
            }
        }
        return markers;
    }

    public Map<String, Set> getPhenotypeDescriptions() {
        Map<String, Set> phenotypes = new HashMap<String, Set>();
        Set<GenotypeExperiment> genotypeExperiments = genotype.getGenotypeExperiments();
        for (GenotypeExperiment genoExperiment : genotypeExperiments) {
            phenotypes.putAll(PhenotypeService.getPhenotypesGroupedByOntology(genoExperiment, anatomyItem));
        }
        return phenotypes;
    }


}
