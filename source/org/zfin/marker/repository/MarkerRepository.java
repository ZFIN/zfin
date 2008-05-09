package org.zfin.marker.repository;

import org.zfin.marker.*;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.Accession;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.orthology.Orthologue;
import org.zfin.publication.Publication;
import org.zfin.people.Person;

import java.util.TreeSet;
import java.util.List;

public interface MarkerRepository {
    public Marker getMarker(Marker marker);
    public Marker getMarkerByID(String zdbID);
    public Marker getMarkerByAbbreviation(String abbreviation);
    public List<Marker> getMarkersByAbbreviation(String name) ;
    public Marker getMarkerByName(String name);
    //Todo: should this move to another class?
    public MarkerRelationship getSpecificMarkerRelationship(Marker firstMarker,
                                                            Marker secondMarker,
                                                            MarkerRelationship.Type type);
    public MarkerRelationship getMarkerRelationshipByID(String zdbID);
    public TreeSet<String> getLG(Marker marker);
    public void addSmallSegmentToGene(Marker segment, Marker gene, String attributionZdbID);
    public void addMarkerDataNote(Marker marker, String note, Person curator);
    public void addMarkerAlias(Marker marker, String alias, String attributionZdbID);

    public void addDBLink(Marker marker, String accessionNumber, ReferenceDatabase refdb, String attributionZdbID);

    public void addOrthoDBLink(Orthologue orthologue, EntrezProtRelation accessionNumber);

    public MarkerHistory getLastMarkerHistory(Marker marker, MarkerHistory.Event event);

    public MarkerHistory createMarkerHistory(Marker newMarker, Marker oldMarker, MarkerHistory.Event event, MarkerHistory.Reason resason);

    public MarkerType getMarkerTypeByName(String name);

    public MarkerTypeGroup getMarkerTypeGroupByName(String name);

    public void renameMarker(Marker marker, Publication publication, MarkerHistory.Reason reason);

    public List<MarkerFamilyName> getMarkerFamilyNamesBySubstring(String substring);
    public MarkerFamilyName getMarkerFamilyName(String name);
    
    public void save(Object o);

    void runMarkerNameFastSearchUpdate(Marker marker);

    void createMarker(Marker marker, Publication publication);

    /**
     * Checks if a gene has a small segment relationship with a given small segment.
     * @param associatedMarker Gene
     * @param smallSegment small segment marker
     * @return boolean
     */
    boolean hasSmallSegmentRelationship(Marker associatedMarker, Marker smallSegment);
}
