package org.zfin.marker;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.zfin.sequence.LinkageGroup;
import org.zfin.sequence.Accession;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.infrastructure.AttributionService;
import org.zfin.infrastructure.ActiveData;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * Sevice Class that deals with Marker related logic.
 */
public class MarkerService {

    static Logger logger = Logger.getLogger(MarkerService.class);

    /**
     * Looks for firstMarkers in Genedom and returns the entire relation.
     *
     * @param marker
     * @return
     */
    public static Set<MarkerRelationship> getRelatedGenedomMarkerRelations(Marker marker) {
        Set<MarkerRelationship> markerRelationships= new HashSet<MarkerRelationship>();
        Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();
        
        if (relationTwo != null){
            for (MarkerRelationship rel : relationTwo){
                if(rel.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)){
                    markerRelationships.add(rel);
                }
            }
        }

        return markerRelationships;
    }

    /**
     * Retrieve a target marker that is related to the source marker
     * via one or more marker relationship types.
     * <p/>
     * Return:
     * If the source marker is null it returns null.
     * If the set of types is null returns all related markers.
     *
     * @param marker source marker
     * @param types  Marker Relationship type
     * @return a set of markers
     */
    public static Set<Marker> getRelatedMarker(Marker marker, Set<MarkerRelationship.Type> types) {
        if (types == null)
            return null;

        Set<Marker> markers = new HashSet<Marker>();
        if (CollectionUtils.isEmpty(types)) {
            Set<MarkerRelationship> relationOne = marker.getFirstMarkerRelationships();
            Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();
            if (relationOne != null)
                for (MarkerRelationship rel : relationOne)
                    markers.add(rel.getSecondMarker());
            if (relationTwo != null)
                for (MarkerRelationship rel : relationTwo)
                    markers.add(rel.getFirstMarker());
        } else {
            Set<MarkerRelationship> relationOne = marker.getFirstMarkerRelationships();
            Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();
            if (relationOne != null) {
                for (MarkerRelationship rel : relationOne) {
                    if (types.contains(rel.getType()))
                        markers.add(rel.getSecondMarker());
                }
            }
            if (relationTwo != null) {
                for (MarkerRelationship rel : relationTwo) {
                    if (types.contains(rel.getType()))
                        markers.add(rel.getFirstMarker());
                }
            }
        }

        return markers;
    }

    /**
     * Retried a target marker that is related to the source marker via a single relationship type
     * (this is a convenience method for passing only a single type into getRelatedMarker(marker, types))
     */
    public static Set<Marker> getRelatedMarker(Marker marker, MarkerRelationship.Type type) {
        Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
        types.add(type);
        return getRelatedMarker(marker, types);
    }


    /**
     * Retrieves 0 or more genes associated with a clone that contains or encodes a small segment.
     *
     * @param clone Marker object
     * @return Set<Marker> object
     */
    public static Set<Marker> getRelatedSmallSegmentGenesFromClone(Marker clone) {
        Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
        types.add(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT);
        types.add(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        Set<Marker> genes = getRelatedMarker(clone, types);

        if (CollectionUtils.isEmpty(genes)) {
            return genes ;
        }

        if(genes.size()>1){
            logger.info("clone "+clone.toString()+" \n has more than one genes associated [" + genes.size()+"]");
        }
        return genes ;
    }


    /**
     * Retrieve an associated gene (of type genedom).
     * If there are more than one gene found it throws a Runtime Exception.
     *
     * @param clone Clone object
     * @return Marker object
     * @deprecated Use getRelatedSmallSegmentGenesFromClone if in RENO interface.
     */
    public static Marker getRelatedGeneFromClone(Marker clone) {
        Set<MarkerRelationship.Type> types = new HashSet<MarkerRelationship.Type>();
        types.add(MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        types.add(MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT);
        types.add(MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        types.add(MarkerRelationship.Type.GENE_HYBRIDIZED_BY_SMALL_SEGMENT);

        Set<Marker> genes = getRelatedMarker(clone, types);

        if (CollectionUtils.isEmpty(genes)) {
            return null;
        }

        logger.info("genes: " + genes);
        logger.info("genes.size: " + genes.size());
        if (genes.size() > 1) {
            throw new RuntimeException("Found more than one gene associated to clone " + clone + ". " +
                    "Found genes: " + genes);
        }
        return genes.iterator().next();
    }


    /**
     * Retrieve LG for clone or gene.
     *
     * If a clone it returns all LinkageGroups contained by or encoded by the clone's small segments.
     *
     * @param marker Marker
     * @return list of LinkageGroups
     */
    public static List<LinkageGroup> getLinkageGroups(Marker marker) {
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        if (marker == null){
            return null;
        }
        
        List<LinkageGroup> groups = new ArrayList<LinkageGroup>();
        // if it is a clone (non-gene) check lg for clone first then the gene.
        Set<String> linkageGroups = mr.getLG(marker);
        if (!marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            // if no linkage group found for clone
            // check the associated gene
            if (CollectionUtils.isEmpty(linkageGroups)) {
//                Marker gene = getRelatedGeneFromClone(marker);
                Set<Marker> genes = getRelatedSmallSegmentGenesFromClone(marker);
                for(Marker gene: genes){
                    if (gene != null) {
//                        linkageGroups = mr.getLG(gene);
                        linkageGroups.addAll(mr.getLG(gene));
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(linkageGroups)) {
            for (String linkage : linkageGroups) {
                LinkageGroup group = new LinkageGroup();
                group.setName(linkage);
                groups.add(group);
            }
        }
        return groups;
    }

    /**  Cleans up dblink records that shouldn't exist.
     *
     *   turns:
     *   accession ---------------------------> gene
     *          `---------> segment -------------^
     *
     *   into:
     *   accession--------> segment ---------> gene
     *
     *  (an accession is connected to a gene the good way, through a marker relationship
     *   and also connected directly.  We want to remove the direct connection)
     *
     *  There is one case where the link is not removed, if the dblink between
     *  the accession & gene is attributed to a journal publication.
     *
     * @param gene Gene to remove unnecessary dblinks from
     * @param accessions Set of accessions to check for redundancy in
     */
    public static void removeRedundantDBLinks(Marker gene, Set<Accession> accessions) {
        Set<Marker> segments = MarkerService.getRelatedMarker(gene, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        //scan through the markers associated with the accession
        for (Accession acc : accessions) {
            for (Marker segment : segments) {
                if (acc.getBlastableMarkers().contains(gene)
                        && acc.getBlastableMarkers().contains(segment)) {
                    //Now we have an accession connected to a gene both directly
                    //and through a marker relationship.
                    for (MarkerDBLink dblink : acc.getBlastableMarkerDBLinks() ) {
                        if (AttributionService.dataSupportedOnlyByCurationPubs(dblink.getZdbID())
                                && dblink.getMarker().equals(gene)) {
                            logger.info("deleting " + dblink.getZdbID()
                                    + " because a marker relationship made the db_link unnecessary");
                            RepositoryFactory.getInfrastructureRepository().deleteActiveDataByZdbID(dblink.getZdbID());
                        }
                    }

                }
            }
        }
    }
}
