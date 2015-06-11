package org.zfin.mutant;

import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.mutant.presentation.GenotypeInformation;

import java.util.*;

public class GenotypeService {

    public static List<GenotypeInformation> getGenotypeInfo(List<Genotype> genotypes) {
        if (genotypes == null)
            return null;
        List<GenotypeInformation> genotypeInformations = new ArrayList<>();
        for (Genotype genoType : genotypes) {
            GenotypeInformation genotypeInformation = new GenotypeInformation(genoType);
            genotypeInformations.add(genotypeInformation);
        }
        return genotypeInformations;
    }

    public static List<GenoExpStatistics> getGenotypeExpStats(List<Genotype> genotypes, Feature fr) {
        if (genotypes == null || fr == null)
            return null;

        List<GenoExpStatistics> stats = new ArrayList<>();
        for (Genotype genoType : genotypes) {
            GenoExpStatistics stat = new GenoExpStatistics(genoType, fr);
            stats.add(stat);
        }
        return stats;
    }

    public static SortedSet<Marker> getAffectedMarker(Genotype genotype) {
        Set<GenotypeFeature> features = genotype.getGenotypeFeatures();
        SortedSet<Marker> markers = new TreeSet<Marker>();
        for (GenotypeFeature feat : features) {
            Feature feature = feat.getFeature();
            Set<FeatureMarkerRelationship> rels = feature.getFeatureMarkerRelations();
            for (FeatureMarkerRelationship rel : rels) {
                if (rel.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                    Marker marker = rel.getMarker();
                    // Only add true genes
                    if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                        markers.add(marker);
                    }
                }
            }
        }
        return markers;
    }

}
