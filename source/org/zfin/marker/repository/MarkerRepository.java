package org.zfin.marker.repository;

import org.zfin.antibody.Antibody;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.*;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.orthology.Orthologue;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.AntibodyStatistics;

import java.util.List;
import java.util.TreeSet;

public interface MarkerRepository {
    Marker getMarker(Marker marker);

    Marker getMarkerByID(String zdbID);

    Marker getMarkerByAbbreviation(String abbreviation);

    Marker getMarkerByName(String name);

    //Todo: should this move to another class?
    MarkerRelationship getSpecificMarkerRelationship(Marker firstMarker,
                                                     Marker secondMarker,
                                                     MarkerRelationship.Type type);

    List<Marker> getMarkersByAbbreviation(String name);

    MarkerRelationship getMarkerRelationshipByID(String zdbID);

    DataAlias getSpecificDataAlias(Marker marker, String alias);

    TreeSet<String> getLG(Marker marker);

    void addMarkerRelationship(Marker marker, Marker gene, String attributionZdbID, MarkerRelationship.Type type);


    void addMarkerDataNote(Marker marker, String note, Person curator);

    void addAntibodyExternalNote(Antibody antibody, String note, String sourcezdbid);

    void editAntibodyExternalNote(String notezdbid, String note);

    /**
     * Create a new alias for a given marker. IF no alias is found no alias is crerated.
     *
     * @param marker valid marker object.
     * @param alias alias string
     * @param publication  publication object
     */
    void addMarkerAlias(Marker marker, String alias, Publication publication);

    /**
     * Delete an existing alias that belongs to a given marker.
     * @param marker Marker Object
     * @param alias Marker alias object
     */
    void deleteMarkerAlias(Marker marker, MarkerAlias alias);

    void addAliasPub(String alias, String attributionZdbID, Marker antibody);

    void addRelPub(String relzdbid, String attributionZdbID, Marker antibody);

    /**
     * Add a publication to a given marker: Attribution.
     * @param marker valid marker object
     * @param publication publication object
     */
    void addMarkerPub(Marker marker,  Publication publication);

    void addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID);

    void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accessionNumber);

    MarkerHistory getLastMarkerHistory(Marker marker, MarkerHistory.Event event);

    MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason);

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
     * Retrieve all markers of a given type group whose abbreviation
     * contains the 'name' string
     *
     * @param name       String
     * @param markerType Marker.MarkerType
     * @return list of marker objects
     */
    List<Marker> getMarkersByAbbreviationAndGroup(String name, Marker.TypeGroup markerType);

    /**
     * Retrieve a marker alias by zdb ID
     * @param aliasZdbID id
     * @return Marker Alias object
     */
    MarkerAlias getMarkerAlias(String aliasZdbID);

    /**
     * Get all high quality probes AO Statistics records for a given ao term.
     * Note: for the case to include substructures the result set is not returned just the total number
     * in the PaginationResult object!
     *
     * @param aoTerm ao term
     * @param pagination pagination bean
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
     * @param typeGroup type group
     * @return list of marker types
     */
    List<MarkerType> getMarkerTypesByGroup(Marker.TypeGroup typeGroup);

}
