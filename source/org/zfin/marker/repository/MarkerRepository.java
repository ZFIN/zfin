package org.zfin.marker.repository;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.DataAlias;
import org.zfin.infrastructure.DataNote;
import org.zfin.marker.*;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.orthology.Orthologue;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;
import java.util.TreeSet;

public interface MarkerRepository {
    Marker getMarker(Marker marker);

    Marker getMarkerByID(String zdbID);

    Clone getCloneById(String zdbID);

    Transcript getTranscriptByZdbID(String zdbID);

    Transcript getTranscriptByName(String name);

    Transcript getTranscriptByVegaID(String vegaID);

    List<String> getTranscriptTypes();

    Marker getMarkerByAbbreviation(String abbreviation);

    Marker getMarkerByName(String name);

    //Todo: should this move to another class?

    MarkerRelationship getMarkerRelationship(Marker firstMarker,
                                             Marker secondMarker,
                                             MarkerRelationship.Type type);

    List<Marker> getMarkersByAbbreviation(String name);

    MarkerRelationship getMarkerRelationshipByID(String zdbID);

    MarkerAlias getSpecificDataAlias(Marker marker, String alias);

    TreeSet<String> getLG(Marker marker);

    MarkerRelationship addMarkerRelationship(MarkerRelationship mrel, String attributionZdbID);

    void addSmallSegmentToGene(Marker gene, Marker segment, String attributionZdbID);

    DataNote addMarkerDataNote(Marker marker, String note, Person curator);

    AntibodyExternalNote addAntibodyExternalNote(Antibody antibody, String note, String sourcezdbid);

    /**
     * Creates a new note in regards to the orthololgy to a gene.
     * Only a single note allowed per gene, i.e. if no note exists then
     * create a new one, if there is already one available then it gets replaced
     * with this one (updated) as we typically display the old note in the updates field.
     * Attribution is done on the current security person.
     *
     * @param gene gene
     * @param note note text
     */
    void createOrUpdateOrthologyExternalNote(Marker gene, String note);

    void editAntibodyExternalNote(String notezdbid, String note);

    /**
     * Create a new alias for a given marker. IF no alias is found no alias is crerated.
     *
     * @param marker      valid marker object.
     * @param alias       alias string
     * @param publication publication object
     */
    MarkerAlias addMarkerAlias(Marker marker, String alias, Publication publication);

    /**
     * Delete an existing alias that belongs to a given marker.
     *
     * @param marker Marker Object
     * @param alias  Marker alias object
     */
    void deleteMarkerAlias(Marker marker, MarkerAlias alias);

    /**
     * Delete a marker relationship
     *
     * @param mrel
     */
    void deleteMarkerRelationship(MarkerRelationship mrel);

    void addDataAliasAttribution(DataAlias alias, Publication attribution, Marker marker);

    void addMarkerRelationshipAttribution(MarkerRelationship mrel, Publication attribution, Marker marker);

    /**
     * Add a publication to a given marker: Attribution.
     *
     * @param marker      valid marker object
     * @param publication publication object
     */
    void addMarkerPub(Marker marker, Publication publication);

    MarkerDBLink getDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb);

    DBLink addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID);

    void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accessionNumber);

    MarkerHistory getLastMarkerHistory(Marker marker, MarkerHistory.Event event);

    MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason, MarkerAlias markerAlias);

    MarkerType getMarkerTypeByName(String name);

    MarkerTypeGroup getMarkerTypeGroupByName(String name);

    void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason);

    List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring);

    MarkerFamilyName getMarkerFamilyName(String name);

    void save(Object o);

    void runMarkerNameFastSearchUpdate(Marker marker);

    void createMarker(Marker marker, Publication publication);

    /**
     * Update marker object. Requires a valid publication.
     *
     * @param marker      new marker object
     * @param publication publication under which the changes are attributed
     * @param alias       the alias name
     */
    void updateMarker(Marker marker, Publication publication, String alias);


    /**
     * Checks if a gene has a small segment relationship with a given small segment.
     *
     * @param associatedMarker Gene
     * @param smallSegment     small segment marker
     * @return boolean
     */
    boolean hasSmallSegmentRelationship(Marker associatedMarker, Marker smallSegment);


    /**
     * @param associatedMarker Associated Marker.
     * @param transcript       Transcript
     * @return Marker has a relationship with this Transcript.
     */
    boolean hasTranscriptRelationship(Marker associatedMarker, Marker transcript);

    /**
     * Retrieve all markers of a given type group whose abbreviation
     * contains the 'name' string
     *
     * @param name       String
     * @param markerType Marker.MarkerType
     * @return list of marker objects
     */
    List<Marker> getMarkersByAbbreviationAndGroup(String name, Marker.TypeGroup markerType);

    // clone methods

    List<String> getPolymeraseNames();

    List<String> getVectorNames();

    List<String> getProbeLibraryNames();

    List<ProbeLibrary> getProbeLibraries();

    ProbeLibrary getProbeLibrary(String zdbID);

    List<String> getDigests();

    List<String> getCloneSites();

    /**
     * Retrieve a marker alias by zdb ID
     *
     * @param aliasZdbID id
     * @return Marker Alias object
     */
    MarkerAlias getMarkerAlias(String aliasZdbID);


    List<TranscriptTypeStatusDefinition> getAllTranscriptTypeStatusDefinitions();

    List<TranscriptType> getAllTranscriptTypes();

    TranscriptType getTranscriptTypeForName(String typeString);

    TranscriptStatus getTranscriptStatusForName(String statusString);

    //these are pulled from push4genomix.pl

    boolean getGeneHasExpression(Marker gene);

    boolean getGeneHasExpressionImages(Marker gene);

    boolean getGeneHasGOEvidence(Marker gene);

    boolean getGeneHasPhenotype(Marker gene);

    boolean getGeneHasPhenotypeImage(Marker gene);


    /**
     * Get all high quality probes AO Statistics records for a given ao term.
     * Note: for the case to include substructures the result set is not returned just the total number
     * in the PaginationResult object!
     *
     * @param aoTerm               ao term
     * @param pagination           pagination bean
     * @param includeSubstructures boolean
     * @return pagination result
     */
    PaginationResult<HighQualityProbe> getHighQualityProbeStatistics(AnatomyItem aoTerm, PaginationBean pagination, boolean includeSubstructures);

    /**
     * Retrieve all distinct publications that contain a high quality probe
     * with a rating of 4.
     *
     * @param anatomyTerm Anatomy Term
     * @return list of publications
     */
    List<Publication> getHighQualityProbePublications(AnatomyItem anatomyTerm);

    /**
     * Retrieve marker types by marker type groups
     *
     * @param typeGroup type group
     * @return list of marker types
     */
    List<MarkerType> getMarkerTypesByGroup(Marker.TypeGroup typeGroup);


    List<Marker> getMarkersForStandardAttributionAndType(Publication publication, String type);

    List<Marker> getMarkerForAttribution(String publicationZdbID);
}
