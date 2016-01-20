package org.zfin.feature.presentation;

import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.gbrowse.presentation.GBrowseImage;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.mutant.GenotypeDisplay;
import org.zfin.mutant.presentation.GenoExpStatistics;
import org.zfin.sequence.FeatureDBLink;

import java.util.List;
import java.util.Set;

public class FeatureBean {
    private Feature feature;
    private Marker marker;
    private int numPubs;
    private List<GenoExpStatistics> genoexpStats;
    private MappedMarkerBean mappedMarkerBean;
    private Set<FeatureMarkerRelationship> sortedConstructRelationships ;
    private FeatureMarkerRelationship createdByRelationship;
    private List<RecordAttribution> featureTypeAttributions ;
    private String singlePublication ;
    private Set<String> featureMap ;
    private Set<String> featureLocations ;
    private String zdbID;
    private Set<FeatureDBLink> summaryPageDbLinks;
    private Set<FeatureDBLink> genbankDbLinks;
    private GBrowseImage gBrowseImage;
    private List<GenotypeDisplay> genotypeDisplays;

    public Set<FeatureDBLink> getGenbankDbLinks() {
        return genbankDbLinks;
    }

    public void setGenbankDbLinks(Set<FeatureDBLink> genbankDbLinks) {
        this.genbankDbLinks = genbankDbLinks;
    }

    public Set<FeatureDBLink> getSummaryPageDbLinks() {
        return summaryPageDbLinks;
    }

    public void setSummaryPageDbLinks(Set<FeatureDBLink> summaryPageDbLinks) {
        this.summaryPageDbLinks = summaryPageDbLinks;
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

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
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

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public FeatureMarkerRelationship getCreatedByRelationship() {
        return createdByRelationship;
    }

    public void setCreatedByRelationship(FeatureMarkerRelationship createdByRelationship) {
        this.createdByRelationship = createdByRelationship;
    }

    public GBrowseImage getgBrowseImage() {
        return gBrowseImage;
    }

    public void setgBrowseImage(GBrowseImage gBrowseImage) {
        this.gBrowseImage = gBrowseImage;
    }

    public List<GenotypeDisplay> getGenotypeDisplays() {
        return genotypeDisplays;
    }

    public void setGenotypeDisplays(List<GenotypeDisplay> genotypeDisplays) {
        this.genotypeDisplays = genotypeDisplays;
    }
}

