package org.zfin.feature.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.FeatGenoStatistics;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

public class FeatureBean {
    private Feature feature;
    private List<Genotype> genotypes;
    private List<FeatGenoStatistics> featgenoStats;
    private Marker marker;
    private int numPubs;
    private List<GenoExpStatistics> genoexpStats;
    private MappedMarkerBean mappedMarkerBean;
    private Set<FeatureMarkerRelationship> sortedMarkerRelationships ;
    private Set<FeatureMarkerRelationship> sortedConstructRelationships ;
    private List<RecordAttribution> featureTypeAttributions ;
    private String singlePublication ;
    private Set<String> featureMap ;
    private Set<String> featureLocations ;
    private String zdbID;

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
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Set<FeatureMarkerRelationship> getSortedMarkerRelationships() {
        return sortedMarkerRelationships;
    }

    public void setSortedMarkerRelationships(Set<FeatureMarkerRelationship> sortedMarkerRelationships) {
        this.sortedMarkerRelationships = sortedMarkerRelationships;
    }

    public Set<FeatureMarkerRelationship> getSortedConstructRelationships() {
        return sortedConstructRelationships;
    }

    public void setSortedConstructRelationships(Set<FeatureMarkerRelationship> sortedConstructRelationships) {
        this.sortedConstructRelationships = sortedConstructRelationships;
    }

    public List<RecordAttribution> getFeatureTypeAttributions() {
        return featureTypeAttributions;
    }

    public void setFeatureTypeAttributions(List<RecordAttribution> featureTypeAttributions) {
        this.featureTypeAttributions = featureTypeAttributions;
    }

    public String getSinglePublication() {
        return singlePublication;
    }

    public void setSinglePublication(String singlePublication) {
        this.singlePublication = singlePublication;
    }

    public Set<String> getFeatureMap() {
        return featureMap;
    }

    public void setFeatureMap(Set<String> featureMap) {
        this.featureMap = featureMap;
    }

    public Set<String> getFeatureLocations() {
        return featureLocations;
    }

    public void setFeatureLocations(Set<String> featureLocations) {
        this.featureLocations = featureLocations;
    }

    public String getDeleteURL() {
        return "";
    }

    public String getEditURL() {

        return "";
    }

    public AuditLogItem getLatestUpdate() {
        if(feature!=null){
            AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
            return alr.getLatestAuditLogItem(feature.getZdbID());
        }
        return null ;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}

