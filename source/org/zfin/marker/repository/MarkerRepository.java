package org.zfin.marker.repository;

import org.zfin.antibody.Antibody;
import org.zfin.infrastructure.DataAlias;
import org.zfin.marker.*;
import org.zfin.orthology.Orthologue;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.ReferenceDatabase;

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

    void addMarkerRelationship(Marker marker, Marker gene, String attributionZdbID,MarkerRelationship.Type type);

   
    void addMarkerDataNote(Marker marker, String note, Person curator);

    void addAntibodyExternalNote(Antibody antibody, String note, String sourcezdbid);

    void editAntibodyExternalNote(String notezdbid, String note);

    void addMarkerAlias(Marker marker, String alias, String attributionZdbID);

    void addAliasPub(String alias, String attributionZdbID, Marker antibody);

    void addRelPub(String relzdbid, String attributionZdbID, Marker antibody);

    void addMarkerPub(Marker marker, String attributionZdbID);

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

    void updateMarker(Marker marker, Publication publication, Boolean dataAlias, String alias);

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
}
