package org.zfin.construct.repository;

import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.fish.WarehouseSummary;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

/**
 * Basic repository to handle fish search requests.
 */
public interface ConstructRepository {

    List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID);
    ConstructRelationship getConstructRelationshipByID(String zdbID);
    ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type);
    void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID);
    void removeConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID);
    ConstructCuration getConstructByID(String zdbID);
    ConstructCuration getConstructByName(String conName);
    void createConstruct(ConstructCuration construct, Publication publication, Person loggedInUser);

    void updateConstructName(String constructZdbID, String newName);

    void createConstruct(ConstructCuration construct, Publication publication);
    List<Marker> getAllConstructs();

    List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID);
    List<ConstructComponent> getConstructComponentsByConstructZdbId(String constructZdbId);
}