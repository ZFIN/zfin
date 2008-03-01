package org.zfin.mutant.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * ToDo: Please add documentation for this class.
 */
public class GenotypeStatistics {

    private Genotype genotype;
    private AnatomyItem anatomyItem;
    private List<Figure> figures;
    private List<Publication> publications;

    public GenotypeStatistics(Genotype genotype, AnatomyItem anatomyItem) {
        this.genotype = genotype;
        this.anatomyItem = anatomyItem;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public int getNumberOfFigures() {
        if (figures == null) {
            PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
            figures = publicationRep.getFiguresByGenoAndAnatomy(genotype, anatomyItem);
        }
        return figures.size();
    }

    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.get(0);
    }

    public int getNumberOfPublications() {
        if (publications == null) {
            PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();
            publications = publicationRep.getPublicationsWithFiguresPerGenotypeAndAnatomy(genotype, anatomyItem);
        }
        return publications.size();
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
                if (marker.getType() == Marker.Type.GENE) {
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
