package org.zfin.feature.presentation;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.FeatGenoStatistics;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.feature.repository.FeatureService;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class FeatureBean  {
    protected FeatureService featureStat;
    private List<Genotype> genotypes;
    protected Feature feature;
    private List<FeatGenoStatistics> featgenoStats;
    private Marker marker;
    private int numPubs ;
    private List<GenoExpStatistics> genoexpStats;
    private MappedMarkerBean mappedMarkerBean;

    public FeatureBean() {
    }

    public List<Genotype> getGenotypes() {
        return genotypes;
    }

    public void setGenotypes(List<Genotype> genotypes) {
        this.genotypes = genotypes;
    }


    public int getNumPubs() {
        return numPubs;
    }

    public void setNumPubs(int numPubs) {
        this.numPubs = numPubs;
    }



    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public MappedMarkerBean getMappedMarkerBean() {
        return mappedMarkerBean;
    }

    public void setMappedMarkerBean(MappedMarkerBean mappedMarkerBean) {
        this.mappedMarkerBean = mappedMarkerBean;
    }



    public List<GenoExpStatistics> getGenoexpStats() {
        return genoexpStats;
    }

    public void setGenoexpStats(List<GenoExpStatistics> genoexpStats) {
        this.genoexpStats = genoexpStats;
    }

    public List<FeatGenoStatistics> getFeatgenoStats() {
        return featgenoStats;
    }

    public void setFeatgenoStats(List<FeatGenoStatistics> featgenoStats) {
        this.featgenoStats = featgenoStats;
    }
    public boolean isMutantsExist() {
        return !CollectionUtils.isEmpty(featgenoStats);
    }


    public Feature getFeature() {
        if (feature == null) {
            feature = new Feature();
        }

        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature=feature;
    }

    public FeatureService getFeatureStat() {
        if (featureStat == null) {
            if (feature == null)
                return null;
            featureStat = new FeatureService(feature);
        }

        return featureStat;
    }

    public void setFeatureStat(FeatureService featureStat) {
        this.featureStat = featureStat;
    }

}

