package org.zfin.mutant;

import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.marker.Marker;

import java.util.*;

/**
 * A genotype display.
 */
public class GenotypeDisplay implements Comparable<GenotypeDisplay> {

    public static final String COMPLEX = "Complex";
    public static final String UNKNOWN = "Unknown";
    public static final String HOMOZYGOUS = "Homozygous";
    public static final String HETEROZYGOUS = "Heterozygous";

    private static final List<String> ZYGOSITY_ORDER = Arrays.asList(
            HOMOZYGOUS,
            HETEROZYGOUS,
            COMPLEX,
            UNKNOWN
    );

    private Genotype genotype;
    private String zygosity;   // this may be different from the zygocity in GenotypeFeature
    private Zygosity dadZygosity;
    private Zygosity momZygosity;

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public String getZygosity() {
        if (zygosity != null) {
            return zygosity;
        }

        if (genotype.getGenotypeFeatures().size() > 1) {
            setZygosity(COMPLEX);
        } else {
            String zygocityName = genotype.getGenotypeFeatures().iterator().next().getZygosity().getName();
            if (zygocityName == null) {
                setZygosity(UNKNOWN);
            } else if (zygocityName.equalsIgnoreCase(HOMOZYGOUS)) {
                setZygosity(HOMOZYGOUS);
            } else if (zygocityName.equalsIgnoreCase(HETEROZYGOUS)) {
                setZygosity(HETEROZYGOUS);
            } else if (zygocityName.equalsIgnoreCase(COMPLEX)) {
                setZygosity(COMPLEX);
            } else if (zygocityName.equalsIgnoreCase(UNKNOWN)) {
                setZygosity(UNKNOWN);
            } else {
                setZygosity(zygocityName);
            }
        }

        return zygosity;
    }

    public void setZygosity(String zygosity) {
        this.zygosity = zygosity;
    }

    public Zygosity getDadZygosity() {
        return dadZygosity;
    }

    public void setDadZygosity(Zygosity dadZygosity) {
        this.dadZygosity = dadZygosity;
    }

    public Zygosity getMomZygosity() {
        return momZygosity;
    }

    public void setMomZygosity(Zygosity momZygosity) {
        this.momZygosity = momZygosity;
    }

    public String getParentalZygosityDisplay() {
        return GenotypeService.getParentalZygosityDisplay(momZygosity, dadZygosity);
    }

    public SortedSet<Marker> getAffectedGenes() {
        Set<Feature> relatedFeatures = new HashSet<>();
        for (GenotypeFeature genoFeature : genotype.getGenotypeFeatures()) {
            relatedFeatures.add(genoFeature.getFeature());
        }
        Set<FeatureMarkerRelationship> featureMarkerRelationships;
        SortedSet<Marker> affectedGenes = new TreeSet<>();
        Marker affectedGene;
        for (Feature feature : relatedFeatures) {
            featureMarkerRelationships = feature.getFeatureMarkerRelations();
            for (FeatureMarkerRelationship featureMarkerRelationship : featureMarkerRelationships) {
                if (featureMarkerRelationship != null) {
                    if (featureMarkerRelationship.getFeatureMarkerRelationshipType().isAffectedMarkerFlag()) {
                        affectedGene = featureMarkerRelationship.getMarker();
                        if (affectedGene.isInTypeGroup(Marker.TypeGroup.GENEDOM) || affectedGene.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)) {
                            affectedGenes.add(affectedGene);
                        }
                    }
                }
            }
        }
        return affectedGenes;
    }

    public int compareTo(GenotypeDisplay other) {
        int thisIndex = ZYGOSITY_ORDER.indexOf(this.getZygosity());
        int otherIndex = ZYGOSITY_ORDER.indexOf(other.getZygosity());
        int compare = Integer.compare(thisIndex, otherIndex);
        if (compare != 0) {
            return compare;
        }
        return this.getGenotype().compareTo(other.getGenotype());
    }
}
