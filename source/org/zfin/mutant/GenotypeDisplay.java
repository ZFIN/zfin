package org.zfin.mutant;

import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.marker.Marker;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A genotype display.
 */
public class GenotypeDisplay implements Comparable<GenotypeDisplay> {

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
        if (zygosity != null)
            return zygosity;

        if (genotype.getGenotypeFeatures().size() > 1) {
            setZygosity("Complex");
        } else {
            String zygocityName = genotype.getGenotypeFeatures().iterator().next().getZygosity().getName();
            if (zygocityName == null) {
                setZygosity("Unknown");
            } else if (zygocityName.equalsIgnoreCase("homozygous")) {
                setZygosity("Homozygous");
            } else if (zygocityName.equalsIgnoreCase("heterozygous")) {
                setZygosity("Heterozygous");
            } else if (zygocityName.equalsIgnoreCase("complex")) {
                setZygosity("Complex");
            } else if (zygocityName.equalsIgnoreCase("unknown")) {
                setZygosity("Unknown");
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
        StringBuilder displayString = new StringBuilder("");
        boolean unknown = true;
        if (momZygosity.getZygositySymbol().length() > 0) {
            displayString.append("&#9792;");
            displayString.append(momZygosity.getZygositySymbol());
            unknown = false;
        }

        if (dadZygosity.getZygositySymbol().length() > 0) {
            displayString.append("&nbsp;");
            displayString.append("&#9794;");
            displayString.append(dadZygosity.getZygositySymbol());
            unknown = false;
        }

        if (unknown)
            return "Unknown";
        else
            return displayString.toString();
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
                       if (affectedGene.isInTypeGroup(Marker.TypeGroup.GENEDOM)|| affectedGene.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)){

                           affectedGenes.add(affectedGene);
                       }
                   }
               }
           }
        }
        return affectedGenes;
    }

    public int compareTo(GenotypeDisplay another) {
        if (another.getZygosity().equalsIgnoreCase("Homozygous") && !getZygosity().equalsIgnoreCase("Homozygous")) {
            return 1;
        } else if (another.getZygosity().equalsIgnoreCase("Heterozygous") && getZygosity().equalsIgnoreCase("Homozygous")) {
            return -1;
        } else if (another.getZygosity().equalsIgnoreCase("Heterozygous") && !getZygosity().equalsIgnoreCase("Heterozygous")) {
            return 1;
        } else if (another.getZygosity().equalsIgnoreCase("Unknown") && (getZygosity().equalsIgnoreCase("Homozygous") || getZygosity().equalsIgnoreCase("Heterozygous"))) {
            return -1;
        } else if (another.getZygosity().equalsIgnoreCase("Unknown") && !getZygosity().equalsIgnoreCase("Unknown")) {
            return 1;
        } else {
            return genotype.compareTo(another.getGenotype());
        }
    }
}
