package org.zfin.feature.repository;

import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.infrastructure.DataNote;
import org.zfin.mapping.FeatureLocation;
import org.zfin.mapping.VariantSequence;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.PreviousNameLight;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.profile.Organization;
import org.zfin.profile.OrganizationFeaturePrefix;
import org.zfin.publication.Publication;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.gff.AssemblyEnum;

import java.util.*;


public interface FeatureRepository {

    public Feature getFeatureByID(String zdbID);

    /**
     * Retrieve a list of all feature for a given publication.
     * Features need to be directly attributed to the publication in question.
     *
     * @param publicationID publication
     * @return list of features
     */
    List<Feature> getFeaturesByPublication(String publicationID);

    List<FeatureMarkerRelationship> getFeatureRelationshipsByPublication(String publicationZdbID);

    List<String> getRelationshipTypesForFeatureType(FeatureTypeEnum featureTypeEnum);

    FeatureTypeGroup getFeatureTypeGroupByName(String name);

    List<Marker> getMarkersForFeatureRelationAndSource(String featureRelationshipName, String publicationZdbID);

    List<FeatureLabEntry> getFeaturesForPrefix(String prefix);

    List<OrganizationFeaturePrefix> getOrganizationFeaturePrefixForPrefix(String prefix);

    List<Organization> getLabsWithFeaturesForPrefix(String prefix);

    /**
     * Save a new Lab object. If it exists a runtime exception is thrown.
     */
    Organization getLabByFeature(Feature ftr);

    FeatureLocation getLocationByFeature(Feature ftr);

    String getPrefixById(int labPrefixID);

    List<FeaturePrefix> getLabPrefixes(String labName);

    List<Feature> getFeaturesWithLocationOnAssembly11();

    List<Feature> getFeaturesWithGenomicMutDets();

    List<Feature> getNonSaFeaturesWithGenomicMutDets();

    List<Feature> getNonSaFeaturesWithGenomicMutDets(Date startDate);

    List<Feature> getNonSaFeaturesWithGenomicMutDets(Date startDate, String featureZdbID);

    List<Feature> getDeletionFeatures();

    List<Feature> getDeletionFeatures(String featureID);

    List<Feature> getFeaturesByType(FeatureTypeEnum featureTypeEnum);

    String getNextZFLineNum();

    String getNextLineNumberForLabPrefix(FeaturePrefix labPrefix);

    String getNextLineNumberForLabPrefix(String labPrefix);

    List<FeaturePrefix> getLabPrefixes(String labName, boolean assignIfEmpty);

    List<FeaturePrefix> getLabPrefixesById(String labZdbID, boolean assignIfEmpty);

    List<FeaturePrefix> getCurrentLabPrefixesById(String labZdbID, boolean assignIfEmpty);


    List<Organization> getLabsOfOriginWithPrefix();

    List<FeaturePrefixLight> getFeaturePrefixWithLabs();


    List<String> getFeatureTypeDisplayNames();

    List<String> getFeatureTypes();

    String getFeatureTypeDisplay(String featureType);

    FeaturePrefix getFeatureLabPrefixID(String labPrefix);

    FeatureAssay addFeatureAssay(Feature feature, Mutagen mutagen, Mutagee mutagee);

    FeatureAssay getFeatureAssay(Feature feature);

    VariantSequence getFeatureVariant(Feature feature);

    String getAALink(Feature feature);

    FeatureLocation getAllFeatureLocationsOnGRCz11(Feature feature);
    FeatureLocation getAllFeatureLocationsForAssembly(AssemblyEnum assembly, Feature feature);

    List<FeatureGenomicMutationDetail> getAllFeatureGenomicMutationDetails();

    List<Marker> getMarkersByFeature(Feature feature);

    List<Marker> getMarkerIsAlleleOf(Feature feature);

    List<Marker> getMarkersPresentForFeature(Feature feature);

    boolean isExistingFeatureTrackingByAbbreviation(String abbreviation);

    TreeSet<String> getFeatureLG(Feature feat);

    /**
     * @param name name of feature
     * @return A list of Features that contain the name in the abbreviation.
     */
    List<Feature> getFeaturesByAbbreviation(String name);

    Feature getFeatureByPrefixAndLineNumber(String prefix, String lineNumber);

    List<Feature> getFeaturesForStandardAttribution(Publication publication);

    Feature getFeatureByAbbreviation(String featureAbbrev);

    FeatureTracking getFeatureByAbbreviationInTrackingTable(String featureAbbrev);

    String getFeatureByIDInTrackingTable(String featTrackingFeatZdbID);


    void deleteFeatureAlias(Feature feature, FeatureAlias alias);

    void deleteFeatureDBLink(Feature feature, DBLink sequence);

    DataNote addFeatureDataNote(Feature feature, String noteData);


    List<String> getAllFeaturePrefixes();

    List<Feature> getSingleAffectedGeneAlleles();

    boolean isSingleAffectedGeneAlleles(Feature feature);

    List<Marker> getConstruct(String featureZdbId);


    Marker getSingleAllelicGene(String featureZdbId);

    String getCurrentPrefixForLab(String labZdbId);

    String setCurrentPrefix(String labZdbId, String prefix);

    List<Feature> getFeaturesForLab(String labZdbId);

    FeaturePrefix setNewLabPrefix(String prefix, String location);

    String getPrefix(String prefix);


    int setLabOfOriginForFeature(Organization existingLabOfOrigin, Feature feature);

    void deleteLabOfOriginForFeature(Feature feature);

    int addLabOfOriginForFeature(Feature feature, String labOfOrigin);

    /**
     * Retrieve all feature ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     *
     * @param firstNIds
     * @return list of ids
     */
    List<String> getAllFeatures(int firstNIds);

    FeaturePrefix getFeaturePrefixByPrefix(String prefix);

    int insertOrganizationPrefix(Organization organization, FeaturePrefix featurePrefix);

    int setNoLabPrefix(String zdbID);

    List<PreviousNameLight> getPreviousNamesLight(Genotype genotype);

    /**
     * Retrieve features that have an allelic relationship with a given marker.
     *
     * @param marker
     * @return
     */
    List<Feature> getFeaturesByMarker(Marker marker);

    List<Feature> getFeaturesByConstruct(Marker marker);

    List<Marker> getConstructsByFeature(Feature feature);

    int deleteFeatureFromTracking(String featureZdbId);

    Set<Feature> getFeaturesCreatedBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent);

    /**
     * Save a new feature and create a standard pub attribution and a feature type attribution
     *
     * @param feature
     * @param publication
     */
    void saveFeature(Feature feature, Publication publication);

    List<String> getMutagensForFeatureType(FeatureTypeEnum featureTypeEnum);

    void update(Feature feature, Set<FeatureTranscriptMutationDetail> addTranscriptAttribution, String publicationID);

    void deleteFeatureProteinMutationDetail(FeatureProteinMutationDetail detail);

    void deleteFeatureGenomicMutationDetail(FeatureGenomicMutationDetail detail);


    Long getFeaturesForLabCount(String zdbID);

    List<Feature> getFeaturesForLab(String zdbID, int i);

    PaginationResult<Feature> getFeaturesForLab(String zdbID, Pagination pagination);

    int getNumberOfFeaturesForConstruct(Marker construct);

    List<Feature> getAllFeatureList(int i);
}
