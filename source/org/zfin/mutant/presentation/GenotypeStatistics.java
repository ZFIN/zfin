package org.zfin.mutant.presentation;

import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * Convenient class to show statistics about phenotypes related to a given AO term..
 */
public class GenotypeStatistics extends EntityStatistics {

    private Genotype genotype;
    private GenericTerm anatomyItem;
    private PaginationResult<Figure> figureResults = null; // null indicates that this has not been populated yet

    public GenotypeStatistics(Genotype genotype) {
        this.genotype = genotype;
    }

    public GenotypeStatistics(Genotype genotype, GenericTerm anatomyItem) {
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

    public boolean isImgInFigure() {
        if (figureResults == null || figureResults.getTotalCount() == 0)  {
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

    public SortedSet<Marker> getAffectedMarkers() {
        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
        SortedSet<Marker> markers = new TreeSet<Marker>();
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


    public Set<PhenotypeStatement> getPhenotypeStatements() {
        Set<PhenotypeStatement> phenotypeStatements = new HashSet<PhenotypeStatement>();
        for (GenotypeExperiment genoExperiment : genotype.getGenotypeExperiments()) {
            phenotypeStatements.addAll(PhenotypeService.getPhenotypeStatements(genoExperiment, anatomyItem));
        }
        return phenotypeStatements;
    }


}
