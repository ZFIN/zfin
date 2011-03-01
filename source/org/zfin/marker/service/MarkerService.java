package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.AttributionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;

/**
 * Sevice Class that deals with Marker related logic.
 */
@Service
public class MarkerService {

    private static Logger logger = Logger.getLogger(MarkerService.class);
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    /**
     * Looks for firstMarkers in Genedom and returns the entire relation.
     *
     * @param marker Marker in firstMarkerRelation.
     * @return Retuns set of marker relationships related by GENEDOM.
     */
    public static Set<MarkerRelationship> getRelatedGenedomMarkerRelations(Marker marker) {
        Set<MarkerRelationship> markerRelationships = new HashSet<MarkerRelationship>();
        Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();

        if (relationTwo != null) {
            for (MarkerRelationship rel : relationTwo) {
                if (rel.getFirstMarker().isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
                    markerRelationships.add(rel);
                }
            }
        }

        return markerRelationships;
    }

    public static SequenceInfo getSequenceInfo(Marker marker) {
        SequenceInfo sequenceInfo = new SequenceInfo();


        if (marker.getDbLinks() != null) {
            logger.debug(marker.getDbLinks().size() + " total marker dblinks");
            for (MarkerDBLink dblink : marker.getDbLinks()) {
                if (dblink.getReferenceDatabase().getForeignDBDataType().getSuperType().equals(ForeignDBDataType.SuperType.SEQUENCE))
                    sequenceInfo.addDBLink(dblink);
            }
        }

        logger.debug(sequenceInfo.size() + " marker linked sequence dblinks");

        return sequenceInfo;
    }


    public static SummaryDBLinkDisplay getSummaryPages(Marker marker) {
        SummaryDBLinkDisplay sp = new SummaryDBLinkDisplay();
        for (DBLink dblink : marker.getDbLinks()) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.SUMMARY_PAGE))
                sp.addDBLink(dblink);
        }
        return sp;
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
     *
     * @param type Type of marker relationship
     * @return Gets a set of related markers by type.
     */
    public static Set<Marker> getRelatedMarker(Marker marker, MarkerRelationship.Type type) {
        TreeSet<MarkerRelationship.Type> types = new TreeSet<MarkerRelationship.Type>();
        types.add(type);
        return getRelatedMarker(marker, types);
    }


    /**
     * Get RelatedMarker TreeSet (ordered) for a specific relationship type.
     * <p/>
     * This queries for the incoming marker in both the first and second positions,
     * often, this will get you exactly what you want.  Use care if you use this
     * method for a situation like overlapping clones, where the marker coming in
     * is likely to occur on both sides of the relationship.
     *
     * @param marker "this" marker, RelatedMarker.getMarker() will get you the "other" marker.
     * @param type   MarkerRelationship type
     * @return ordered set of RelatedMarkers
     */
    public static TreeSet<RelatedMarker> getRelatedMarkers(Marker marker, MarkerRelationship.Type type) {
        TreeSet<RelatedMarker> relatedMarkers = new TreeSet<RelatedMarker>();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            if (mrel.getType().equals(type))
                relatedMarkers.add(new RelatedMarker(marker, mrel));
        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            if (mrel.getType().equals(type)) {
                relatedMarkers.add(new RelatedMarker(marker, mrel));
            }
        }

        return relatedMarkers;
    }


    public static RelatedMarkerDisplay getRelatedMarkerDisplay(Marker marker) {
        RelatedMarkerDisplay rmd = new RelatedMarkerDisplay();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            rmd.addRelatedMarker(new RelatedMarker(marker, mrel));
        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            rmd.addRelatedMarker(new RelatedMarker(marker, mrel));
        }

        return rmd;
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
            return genes;
        }

        if (genes.size() > 1) {
            logger.info("clone " + clone.toString() + " \n has more than one genes associated [" + genes.size() + "]");
        }
        return genes;
    }


    /**
     * Retrieve LG for clone or gene.
     * <p/>
     * If a clone it returns all LinkageGroups contained by or encoded by the clone's small segments.
     *
     * @param marker Marker
     * @return list of LinkageGroups
     */
    public static Set<LinkageGroup> getLinkageGroups(Marker marker) {
        LinkageRepository lr = RepositoryFactory.getLinkageRepository();
        if (marker == null) {
            return null;
        }

        Set<LinkageGroup> groups = new TreeSet<LinkageGroup>();
        // if it is a clone (non-gene) check lg for clone first then the gene.
        Set<String> linkageGroups = lr.getLG(marker);
        if (marker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
            // if no linkage group found for transcript
            // check the associated gene
            if (CollectionUtils.isEmpty(linkageGroups)) {
//                Marker gene = getRelatedGeneFromClone(marker);
//                Set<Marker> genes = getRelatedSmallSegmentGenesFromClone(marker);S

                Set<Marker> genes = TranscriptService.getRelatedGenesFromTranscript(markerRepository.getTranscriptByZdbID(marker.getZdbID()));
                for (Marker gene : genes) {
                    if (gene != null) {
//                        linkageGroups = mr.getLG(gene);
                        linkageGroups.addAll(lr.getLG(gene));
                    }
                }
            }
        } else if (!marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            // if no linkage group found for clone
            // check the associated gene
            if (CollectionUtils.isEmpty(linkageGroups)) {
//                Marker gene = getRelatedGeneFromClone(marker);
                Set<Marker> genes = getRelatedSmallSegmentGenesFromClone(marker);
                for (Marker gene : genes) {
                    if (gene != null) {
//                        linkageGroups = mr.getLG(gene);
                        linkageGroups.addAll(lr.getLG(gene));
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

    public static MappedMarkerBean getMappedMarkers(Marker marker) {
        LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository();

        MappedMarkerBean mappedMarkerBean = new MappedMarkerBean();
        List<String> directMappedMarkers = linkageRepository.getDirectMappedMarkers(marker);
        mappedMarkerBean.setHasMappedMarkers((directMappedMarkers.size() > 0));
        mappedMarkerBean.setMarker(marker);
        mappedMarkerBean.setUnMappedMarkers(new ArrayList<String>(linkageRepository.getLG(marker)));

        return mappedMarkerBean;
    }

    /**
     * Cleans up dblink records that shouldn't exist.
     * <p/>
     * turns:
     * accession ---------------------------> gene
     * `---------> segment -------------^
     * <p/>
     * into:
     * accession--------> segment ---------> gene
     * <p/>
     * (an accession is connected to a gene the good way, through a marker relationship
     * and also connected directly.  We want to remove the direct connection)
     * <p/>
     * There is one case where the link is not removed, if the dblink between
     * the accession & gene is attributed to a journal publication.
     *
     * @param gene       Gene to remove unnecessary dblinks from
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
                    for (MarkerDBLink dblink : acc.getBlastableMarkerDBLinks()) {
                        if (AttributionService.dataSupportedOnlyByCurationPubs(dblink.getZdbID())
                                && dblink.getMarker().equals(gene)) {
                            logger.info("deleting " + dblink.getZdbID()
                                    + " because a marker relationship made the db_link unnecessary");
                            infrastructureRepository.deleteActiveDataByZdbID(dblink.getZdbID());
                        }
                    }

                }
            }
        }
    }

    /**
     * Adds an attribution for a marker relationship of a given type.
     *
     * @param marker1                First marker in marker relationship.
     * @param marker2                Second marker in marker relationship.
     * @param pubZdbID               Attribute Pub ZdbID.
     * @param markerRelationshipType Marker relationship type to create.
     */
    public static void addMarkerRelationship(Marker marker1, Marker marker2, String pubZdbID,
                                             MarkerRelationship.Type markerRelationshipType) {
        // adds the marker relation and attributes it
//        MarkerRelationship markerRelationship = RepositoryFactory.getMarkerRepository().getSpecificMarkerRelationship(marker1,marker2,markerRelationshipType) ;
        MarkerRelationship markerRelationship = new MarkerRelationship();
        markerRelationship.setFirstMarker(marker1);
        markerRelationship.setSecondMarker(marker2);
        markerRelationship.setType(markerRelationshipType);
        // also inserts attribution
        markerRepository.addMarkerRelationship(markerRelationship, pubZdbID);
    }


    /**
     * Adds an attribution for a marker relationship of a given type.
     *
     * @param marker1                First marker in marker relationship.
     * @param marker2                Second marker in marker relationship.
     * @param pubZdbID               Attribute Pub ZdbID.
     * @param markerRelationshipType Marker relationship type to create.
     */
    public static void addMarkerRelationshipAttribution(Marker marker1, Marker marker2, String pubZdbID,
                                                        MarkerRelationship.Type markerRelationshipType) {
        MarkerRelationship markerRelationship = RepositoryFactory.getMarkerRepository().getMarkerRelationship(marker1, marker2, markerRelationshipType);
        //now deal with attribution
        if (pubZdbID != null && pubZdbID.length() > 0) {
            infrastructureRepository.insertRecordAttribution(markerRelationship.getZdbID(), pubZdbID);
        }
    }

    /**
     * Removes a reference of marker relationship of a given type.
     *
     * @param marker1                First marker in marker relationship.
     * @param marker2                Second marker in marker relationship.
     * @param pubZdbID               Attribute Pub ZdbID.
     * @param markerRelationshipType Marker relationship type to remove.
     */
    public static void deleteMarkerRelationshipAttribution(Marker marker1, Marker marker2, String pubZdbID,
                                                           MarkerRelationship.Type markerRelationshipType) {
        MarkerRelationship markerRelationship = markerRepository.getMarkerRelationship(marker1, marker2, markerRelationshipType);

        //now deal with attribution
        if (pubZdbID != null && pubZdbID.length() > 0) {
            int deletedRecord = infrastructureRepository.deleteRecordAttribution(markerRelationship.getZdbID(), pubZdbID);
            logger.info("deleted record attrs: " + deletedRecord);
        }
    }


    public static void deleteMarkerRelationship(MarkerRelationship mrel) {
        RepositoryFactory.getMarkerRepository().deleteMarkerRelationship(mrel);
    }

    /**
     * Strange that it takes ids as strings, but until we have full access to entity objects in gwt,
     * this makes the code quite a bit smaller
     *
     * @param marker1 marker in first position
     * @param marker2 marker in second position
     * @param type    type of relationship
     */
    public static void deleteMarkerRelationship(Marker marker1, Marker marker2, MarkerRelationship.Type type) {
        MarkerRelationship mrel = RepositoryFactory.getMarkerRepository().getMarkerRelationship(marker1, marker2, type);
        deleteMarkerRelationship(mrel);
    }

    public static Clone createClone(CloneAddBean cloneAddBean) {
        Clone clone = new Clone();
        clone.setName(cloneAddBean.getName());
        clone.setAbbreviation(cloneAddBean.getName());
        clone.setProbeLibrary(RepositoryFactory.getMarkerRepository().getProbeLibrary(cloneAddBean.getLibraryZdbID()));

        Person person = RepositoryFactory.getProfileRepository().getPerson(cloneAddBean.getOwnerZdbID());
        clone.setOwner(person);

        // set marker types
        Marker.Type markerType = Marker.Type.getType(cloneAddBean.getMarkerType());
        MarkerType realMarkerType = new MarkerType();
        realMarkerType.setName(cloneAddBean.getMarkerType());
        realMarkerType.setType(markerType);
        Set<Marker.TypeGroup> typeGroup = new HashSet<Marker.TypeGroup>();
        typeGroup.add(Marker.TypeGroup.getType(cloneAddBean.getMarkerType()));
        realMarkerType.setTypeGroups(typeGroup);
        clone.setMarkerType(realMarkerType);

        HibernateUtil.currentSession().save(clone);

        return clone;
    }

    public static Set<Publication> getAliasAttributions(Marker marker) {
        Set<Publication> publications = new HashSet<Publication>();

        Set<MarkerAlias> mrkrAliases = marker.getAliases();
        if (mrkrAliases != null && !mrkrAliases.isEmpty()) {
            for (MarkerAlias alias : marker.getAliases()) {
                Set<PublicationAttribution> aliasPubs = alias.getPublications();
                if (aliasPubs != null && !aliasPubs.isEmpty()) {
                    for (PublicationAttribution pubAttr : aliasPubs)
                        publications.add(pubAttr.getPublication());
                }
            }
        }

        return publications;
    }

    public static Set<Publication> getMarkerRelationshipAttributions(Marker marker) {
        Set<Publication> publications = new HashSet<Publication>();
        Set<MarkerRelationship> allRelationships = new HashSet<MarkerRelationship>();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            allRelationships.add(mrel);
        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            allRelationships.add(mrel);
        }

        for (MarkerRelationship mrel : allRelationships) {
            Set<PublicationAttribution> mrelPubs = mrel.getPublications();
            if (mrelPubs != null && !mrelPubs.isEmpty()) {
                for (PublicationAttribution pubAttr : mrelPubs)
                    publications.add(pubAttr.getPublication());
            }
        }

        return publications;
    }

    public static Set<Publication> getDBLinkPublicaions(Marker marker) {
        Set<Publication> publications = new HashSet<Publication>();

        for (DBLink dblink : marker.getDbLinks()) {
            Set<PublicationAttribution> dblinkPubs = dblink.getPublications();
            if (dblinkPubs != null && !dblinkPubs.isEmpty()) {
                for (PublicationAttribution pubAttr : dblinkPubs)
                    publications.add(pubAttr.getPublication());
            }
        }

        return publications;
    }


    public static List<String> getCloneMarkerTypes() {
        // set clone marker types
        List<String> typeList = new ArrayList<String>();
        typeList.add(Marker.Type.BAC.toString());
        typeList.add(Marker.Type.PAC.toString());
        typeList.add(Marker.Type.FOSMID.toString());

        typeList.add(Marker.Type.EST.toString());
        typeList.add(Marker.Type.CDNA.toString());
        return typeList;
    }

    public static SummaryDBLinkDisplay getProteinProductDBLinks(Marker marker) {
        SummaryDBLinkDisplay sp = new SummaryDBLinkDisplay();
        for (DBLink dblink : marker.getDbLinks()) {
            if (dblink.isInDisplayGroup(DisplayGroup.GroupName.DISPLAYED_PROTEIN_SEQUENCE))
                sp.addDBLink(dblink);
        }

        return sp;
    }


    public static List<String> getSuppliers(Marker marker) {
        Set<MarkerSupplier> markerSuppliers = marker.getSuppliers();
        List<String> supplierList = new ArrayList<String>();
        for (MarkerSupplier markerSupplier : markerSuppliers) {
            supplierList.add(markerSupplier.getOrganization().getName());
        }
        return supplierList;
    }

    public static List<String> getDirectAttributions(Marker marker) {
        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(marker.getZdbID());
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        List<String> attributions = new ArrayList<String>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        return attributions;
    }
}
