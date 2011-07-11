package org.zfin.feature.repository;

import org.zfin.feature.*;
import org.zfin.feature.presentation.FeatureLabEntry;
import org.zfin.feature.presentation.FeaturePrefixLight;
import org.zfin.gwt.root.dto.FeatureTypeEnum;
import org.zfin.gwt.root.dto.Mutagee;
import org.zfin.gwt.root.dto.Mutagen;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.marker.Marker;
import org.zfin.people.Lab;
import org.zfin.people.LabFeaturePrefix;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.DBLink;

import java.util.List;
import java.util.TreeSet;


public interface FeatureRepository {

    public Feature getFeatureByID(String zdbID);

    DataAlias getSpecificDataAlias(Feature feature, String alias);

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

    List<Marker> getMarkersForFeatureRelationAndSource(String featureRelationshipName, String publicationZdbID);

    List<FeatureLabEntry> getFeaturesForPrefix(String prefix);

    List<LabFeaturePrefix> getLabFeaturePrefixForPrefix(String prefix);

    List<Lab> getLabsWithFeaturesForPrefix(String prefix);

    /**
     * Save a new Lab object. If it exists a runtime exception is thrown.
     */
    Lab getLabByFeature(Feature ftr);

    String getPrefix(int labPrefixID);

    List<FeaturePrefix> getLabPrefixes(String labName);

    List<Lab> getLabsOfOriginWithPrefix();

    List<FeaturePrefixLight> getFeaturePrefixWithLabs();


    List<String> getFeatureTypeDisplayNames();

    List<String> getFeatureTypes();

    String getFeatureTypeDisplay(String featureType);

    FeaturePrefix getFeatureLabPrefixID(String labPrefix);

    FeatureAssay addFeatureAssay(Feature feature, Mutagen mutagen, Mutagee mutagee);

    FeatureAssay getFeatureAssay(String zdbID);

    List<Marker> getMarkersByFeature(Feature feature);

    List<Marker> getMarkersPresentForFeature(Feature feature);

    TreeSet<String> getFeatureLG(Feature feat);

    /**
     * @param name name of feature
     * @return A list of Features that contain the name in the abbreviation.
     */
    List<Feature> getFeaturesByAbbreviation(String name);

    Feature getFeatureByPrefixAndLineNumber(String prefix, String lineNumber);

    List<Feature> getFeaturesForStandardAttribution(Publication publication);

    List<Feature> getFeatureForAttribution(String publicationZdbID);

    List<Feature> getFeaturesForAttribution(String publicationZdbID);

    Feature getFeatureByAbbreviation(String featureAbbrev);

    void deleteFeatureAlias(Feature feature, FeatureAlias alias);
    void deleteFeatureDBLink(Feature feature, DBLink sequence);

    DataNote addFeatureDataNote(Feature feature, String noteData, Person person);

    List<String> getAllFeaturePrefixes();

    String getCurrentPrefixForLab(String labZdbId);

    String setCurrentLabPrefix(String labZdbId, String prefix);

    List<Feature> getFeaturesForLab(String labZdbId);

    FeaturePrefix setNewLabPrefix(String prefix, String location);


    int setLabOfOriginForFeature(Lab existingLabOfOrigin, Feature feature);

    void deleteLabOfOriginForFeature(Feature feature);

    int addLabOfOriginForFeature(Feature feature, String labOfOrigin);

    /**
     * Retrieve all feature ids.
     * If firstNIds > 0 return only the first N.
     * If firstNIds < 0 return null
     * @param firstNIds
     * @return list of ids
     */
    List<String> getAllFeatures(int firstNIds);
}
