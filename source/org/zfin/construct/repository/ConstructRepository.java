package org.zfin.construct.repository;

import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.fish.WarehouseSummary;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

/**
 * Basic repository to handle fish search requests.
 */
public interface ConstructRepository {

    WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart);

    /**
     * Retrieve the status of the fish mart:
     * true: fish mart ready for usage
     * false: fish mart is being rebuilt.
     *
     * @return status
     */
    ZdbFlag getConstructMartStatus();

    List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID);
    ConstructRelationship getConstructRelationshipByID(String zdbID);
    ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type);
    void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID);
    ConstructCuration getConstructByID(String zdbID);
    ConstructCuration getConstructByName(String conName);
    void createConstruct(ConstructCuration construct, Publication publication);

    List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID);
}