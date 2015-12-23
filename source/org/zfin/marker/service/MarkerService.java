package org.zfin.marker.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
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
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermExternalReference;
import org.zfin.ontology.presentation.DiseaseDisplay;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.orthology.presentation.OrthologEvidencePresentation;
import org.zfin.orthology.presentation.OrthologyPresentationRow;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.Organization;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.Database;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zfin.repository.RepositoryFactory.*;

/**
 * Sevice Class that deals with Marker related logic.
 */
@Service
public class MarkerService {

    private static Logger logger = Logger.getLogger(MarkerService.class);
    private static MarkerRepository markerRepository = getMarkerRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    private static MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();

    private static Pattern typePattern = Pattern.compile("ZDB-([\\p{Alpha}_]+)-.*");

    /**
     * Looks for firstMarkers in Genedom and returns the entire relation.
     *
     * @param marker Marker in firstMarkerRelation.
     * @return Retuns set of marker relationships related by GENEDOM.
     */
    public static Set<MarkerRelationship> getRelatedGenedomMarkerRelations(Marker marker) {
        Set<MarkerRelationship> markerRelationships = new HashSet<>();
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

    /**
     * Called on a a marker summary page.
     */
    public static SequenceInfo getSequenceInfoSummary(Marker marker) {

        SequenceInfo sequenceInfo = new SequenceInfo();

        sequenceInfo.setDbLinks(RepositoryFactory.getSequenceRepository()
                        .getDBLinksForMarkerAndDisplayGroup(marker
                                , DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE)
        );

        List<RelatedMarkerDBLinkDisplay> relatedLinks = RepositoryFactory.getSequenceRepository()
                .getDBLinksForFirstRelatedMarker(marker,
                        DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                        MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
                );
        for (RelatedMarkerDBLinkDisplay relatedLink : relatedLinks) {
            sequenceInfo.addDBLink(relatedLink.getLink());
        }

        Set<RelatedMarkerDBLinkDisplay> markerDBLinks = new TreeSet<>();
        markerDBLinks.addAll(RepositoryFactory.getSequenceRepository()
                        .getDBLinksForSecondRelatedMarker(marker
                                , DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE
                                , MarkerRelationship.Type.CLONE_CONTAINS_GENE
                        )
        );

        markerDBLinks.addAll(getTranscriptReferences(marker));
        for (RelatedMarkerDBLinkDisplay relatedLink : markerDBLinks) {
            sequenceInfo.addDBLink(relatedLink.getLink());
        }

        // TODO: this should use a single SQL statement?
        // but this is a bit unmanageable unless it is really slow
//        sequenceInfo.setNumberDBLinks(RepositoryFactory.getSequenceRepository().getNumberDBLinks(marker));
        sequenceInfo.setNumberDBLinks(sequenceInfo.getDbLinks().size());

        List<DBLink> dbLinkSet = new ArrayList<>();
        Set<String> types = new HashSet<>();
        for (DBLink dbLink : sequenceInfo.getDbLinks()) {
            String type = dbLink.getReferenceDatabase().getForeignDBDataType().getDataType().toString();
            if (!types.contains(type)) {
                types.add(type);
                dbLinkSet.add(dbLink);
            } else if (types.contains(type)) {
                sequenceInfo.setHasMoreLinks(true);
            }
        }

        sequenceInfo.setDbLinks(dbLinkSet);


        return sequenceInfo;
    }

    /**
     * To be called to display full page as on sequence-view
     *
     * @param marker
     * @return
     */
    public static SequencePageInfoBean getSequenceInfoFull(Marker marker) {
        SequencePageInfoBean sequenceInfo = new SequencePageInfoBean();
        sequenceInfo.addDBLinks(RepositoryFactory.getSequenceRepository().getDBLinksForMarker(marker.getZdbID(), ForeignDBDataType.SuperType.SEQUENCE));

        List<RelatedMarkerDBLinkDisplay> relatedLinks = RepositoryFactory.getSequenceRepository()
                .getDBLinksForFirstRelatedMarker(marker,
                        DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                        MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
                        MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
                );
        relatedLinks.addAll(RepositoryFactory.getSequenceRepository()
                        .getDBLinksForSecondRelatedMarker(marker
                                , DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE
                                , MarkerRelationship.Type.CLONE_CONTAINS_GENE
                        )
        );
        relatedLinks.addAll(getTranscriptReferences(marker));
        for (RelatedMarkerDBLinkDisplay relatedLink : relatedLinks) {
            sequenceInfo.addRelatedMarkerDBLink(relatedLink.getRelationshipType(), relatedLink.getLink());
        }

        return sequenceInfo;
    }

    public static Collection<RelatedMarkerDBLinkDisplay> getTranscriptReferences(Marker marker) {
        List<RelatedMarkerDBLinkDisplay> relatedMarkerDBLinks = new ArrayList<>();
        List<MarkerDBLink> markerDBLinks = new ArrayList<>();
        String relationship = "";
        if (marker.isInTypeGroup(Marker.TypeGroup.GENEDOM)) {
            markerDBLinks.addAll(RepositoryFactory.getSequenceRepository()
                    .getWeakReferenceDBLinks(marker
                            , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                            , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                    ));
            relationship = "Contained in";
        } else if (marker.isInTypeGroup(Marker.TypeGroup.CLONE)) {
            markerDBLinks.addAll(RepositoryFactory.getSequenceRepository()
                    .getWeakReferenceDBLinks(marker
                            , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                            , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                    ));
            relationship = "Contains";
        }
        for (MarkerDBLink link : markerDBLinks) {
            RelatedMarkerDBLinkDisplay display = new RelatedMarkerDBLinkDisplay();
            display.setLink(link);
            display.setRelationshipType(relationship);
            relatedMarkerDBLinks.add(display);
        }
        return relatedMarkerDBLinks;
    }

    public static SequenceInfo getSequenceInfo(Marker marker) {
        SequenceInfo sequenceInfo = new SequenceInfo();


        if (marker.getDbLinks() != null) {
            logger.debug(marker.getDbLinks().size() + " total marker dblinks");
            for (MarkerDBLink dblink : marker.getDbLinks()) {
                if (dblink.getReferenceDatabase().getForeignDBDataType().getSuperType().equals(ForeignDBDataType.SuperType.SEQUENCE) && !dblink.isInDisplayGroup(DisplayGroup.GroupName.HIDDEN_DBLINKS)) {
                    sequenceInfo.addDBLink(dblink);
                }
            }
        }

        logger.debug(sequenceInfo.getDbLinks().size() + " marker linked sequence dblinks");

        return sequenceInfo;
    }


    public static SummaryDBLinkDisplay getMarkerDBLinkDisplay(Marker marker, DisplayGroup.GroupName groupName) {
        SummaryDBLinkDisplay sp = new SummaryDBLinkDisplay();
        for (DBLink dblink : marker.getDbLinks()) {
            if (dblink.isInDisplayGroup(groupName)) {
                sp.addDBLink(dblink);
            }
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
    public static PaginationResult<Marker> getRelatedMarker(Marker marker, Set<MarkerRelationship.Type> types, Integer numOfRecords) {
        if (marker == null) {
            return null;
        }
        PaginationBean paginationBean = new PaginationBean();
        if (numOfRecords < 0) {
            paginationBean.setMaxDisplayRecords(Integer.MAX_VALUE);
        } else {
            paginationBean.setMaxDisplayRecords(numOfRecords);
        }
        return getMarkerRepository().getRelatedMarker(marker, types, paginationBean);
    }

    public static Set<Marker> getRelatedMarker(Marker marker, Set<MarkerRelationship.Type> types) {
        if (types == null) {
            return null;
        }

        Set<Marker> markers = new TreeSet<>();
        if (CollectionUtils.isEmpty(types)) {
            Set<MarkerRelationship> relationOne = marker.getFirstMarkerRelationships();
            Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();
            if (relationOne != null) {
                for (MarkerRelationship rel : relationOne) {
                    markers.add(rel.getSecondMarker());
                }
            }
            if (relationTwo != null) {
                for (MarkerRelationship rel : relationTwo) {
                    markers.add(rel.getFirstMarker());
                }
            }
        } else {
            Set<MarkerRelationship> relationOne = marker.getFirstMarkerRelationships();
            Set<MarkerRelationship> relationTwo = marker.getSecondMarkerRelationships();
            if (relationOne != null) {
                for (MarkerRelationship rel : relationOne) {
                    if (types.contains(rel.getType())) {
                        markers.add(rel.getSecondMarker());
                    }
                }
            }
            if (relationTwo != null) {
                for (MarkerRelationship rel : relationTwo) {
                    if (types.contains(rel.getType())) {
                        markers.add(rel.getFirstMarker());
                    }
                }
            }
        }

        return markers;
    }

    /**
     * Retried a target marker that is related to the source marker via a single relationship type
     * (this is a convenience method for passing only a single type into getRelatedMarker(marker, types))
     *
     * @param marker Marker to get relationships from.
     * @param type   Type of marker relationship
     * @return Gets a set of related markers by type.
     */
    public static Set<Marker> getRelatedMarker(Marker marker, MarkerRelationship.Type type) {
        TreeSet<MarkerRelationship.Type> types = new TreeSet<>();
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
        TreeSet<RelatedMarker> relatedMarkers = new TreeSet<>();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            if (mrel.getType().equals(type)) {
                relatedMarkers.add(new RelatedMarker(marker, mrel));
            }
        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            if (mrel.getType().equals(type)) {
                relatedMarkers.add(new RelatedMarker(marker, mrel));
            }
        }

        return relatedMarkers;
    }

    public static List<MarkerRelationshipPresentation> getRelatedMarkerDisplayExcludeType(Marker marker, final boolean is1to2) {
        return markerRepository.getRelatedMarkerOrderDisplayExcludeTypes(marker, is1to2
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
                , MarkerRelationship.Type.PROMOTER_OF
                , MarkerRelationship.Type.CODING_SEQUENCE_OF
                , MarkerRelationship.Type.CONTAINS_ENGINEERED_REGION
                , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY
                , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                , MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE
        );
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
        Set<MarkerRelationship.Type> types = new HashSet<>();
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

        Set<LinkageGroup> groups = new TreeSet<>();
        // if it is a clone (non-gene) check lg for clone first then the gene.
        Set<String> linkageGroups = lr.getChromosomeLocations(marker);
        if (marker.isInTypeGroup(Marker.TypeGroup.TRANSCRIPT)) {
            // if no linkage group found for transcript
            // check the associated gene
            if (CollectionUtils.isEmpty(linkageGroups)) {
//                Marker gene = getRelatedGeneFromClone(marker);
//                Set<Marker> genes = getRelatedSmallSegmentGenesFromClone(marker);S

                Set<Marker> genes = TranscriptService.getRelatedGenesFromTranscript(markerRepository.getTranscriptByZdbID(marker.getZdbID()));
                for (Marker gene : genes) {
                    if (gene != null) {
//                        linkageGroups = mr.getChromosomeLocations(gene);
                        linkageGroups.addAll(lr.getChromosomeLocations(gene));
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
//                        linkageGroups = mr.getChromosomeLocations(gene);
                        linkageGroups.addAll(lr.getChromosomeLocations(gene));
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
        mappedMarkerBean.setUnMappedMarkers(new ArrayList<>(linkageRepository.getChromosomeLocations(marker)));

        return mappedMarkerBean;
    }

    /**
     * From mapping details:
     * select mrel_mrkr_1_zdb_id
     * from marker_relationship
     * where mrel_mrkr_2_zdb_id = '$oID'
     * and mrel_type = 'contains polymorphism'
     * and (mrel_mrkr_1_zdb_id[1,8] = 'ZDB-EST-'
     * or mrel_mrkr_1_zdb_id[1,8] = 'ZDB-GENE'); ">
     *
     * @param marker
     * @return
     */
    public static MappedMarkerBean getSnpMappedMarkers(Marker marker) {
        Set<Marker> relatedMarkers = getRelatedMarker(marker, MarkerRelationship.Type.CONTAINS_POLYMORPHISM);

        if (CollectionUtils.isEmpty(relatedMarkers)) {
            return new MappedMarkerBean();
        } else {
            return getMappedMarkers(relatedMarkers.iterator().next());
        }
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
        Set<Marker> segments = getRelatedMarker(gene, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

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
    public static MarkerRelationship addMarkerRelationship(Marker marker1, Marker marker2, String pubZdbID,
                                                           MarkerRelationship.Type markerRelationshipType) {
        // adds the marker relation and attributes it
//        MarkerRelationship markerRelationship = RepositoryFactory.getMarkerRepository().getSpecificMarkerRelationship(marker1,marker2,markerRelationshipType) ;
        MarkerRelationship markerRelationship = new MarkerRelationship();
        markerRelationship.setFirstMarker(marker1);
        markerRelationship.setSecondMarker(marker2);
        markerRelationship.setType(markerRelationshipType);
        // also inserts attribution
        return markerRepository.addMarkerRelationship(markerRelationship, pubZdbID);
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
        MarkerRelationship markerRelationship = getMarkerRepository().getMarkerRelationship(marker1, marker2, markerRelationshipType);
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
        getMarkerRepository().deleteMarkerRelationship(mrel);
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
        MarkerRelationship mrel = getMarkerRepository().getMarkerRelationship(marker1, marker2, type);
        deleteMarkerRelationship(mrel);
    }

    public static Clone createClone(CloneAddBean cloneAddBean) {
        Clone clone = new Clone();
        clone.setName(cloneAddBean.getName());
        clone.setAbbreviation(cloneAddBean.getName());
        clone.setProbeLibrary(getMarkerRepository().getProbeLibrary(cloneAddBean.getLibraryZdbID()));
        clone.setOwner(ProfileService.getCurrentSecurityUser());

        // set marker types
        Marker.Type markerType = Marker.Type.getType(cloneAddBean.getMarkerType());
        MarkerType realMarkerType = new MarkerType();
        realMarkerType.setName(cloneAddBean.getMarkerType());
        realMarkerType.setType(markerType);
        Set<Marker.TypeGroup> typeGroup = new HashSet<>();
        typeGroup.add(Marker.TypeGroup.getType(cloneAddBean.getMarkerType()));
        realMarkerType.setTypeGroups(typeGroup);
        clone.setMarkerType(realMarkerType);

        HibernateUtil.currentSession().save(clone);

        return clone;
    }

    public static Set<Publication> getAliasAttributions(Marker marker) {
        Set<Publication> publications;
        publications = new HashSet<>();

        Set<MarkerAlias> mrkrAliases = marker.getAliases();
        if (mrkrAliases != null && !mrkrAliases.isEmpty()) {
            for (MarkerAlias alias : marker.getAliases()) {
                Set<PublicationAttribution> aliasPubs = alias.getPublications();
                if (aliasPubs != null && !aliasPubs.isEmpty()) {
                    for (PublicationAttribution pubAttr : aliasPubs) {
                        publications.add(pubAttr.getPublication());
                    }
                }
            }
        }

        return publications;
    }

    public static Set<Publication> getMarkerRelationshipAttributions(Marker marker) {
        Set<Publication> publications = new HashSet<>();
        Set<MarkerRelationship> allRelationships = new HashSet<>();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            allRelationships.add(mrel);
        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            allRelationships.add(mrel);
        }

        for (MarkerRelationship mrel : allRelationships) {
            Set<PublicationAttribution> mrelPubs = mrel.getPublications();
            if (mrelPubs != null && !mrelPubs.isEmpty()) {
                for (PublicationAttribution pubAttr : mrelPubs) {
                    publications.add(pubAttr.getPublication());
                }
            }
        }

        return publications;
    }

    public static Set<Publication> getDBLinkPublicaions(Marker marker) {
        Set<Publication> publications = new HashSet<>();

        for (DBLink dblink : marker.getDbLinks()) {
            Set<PublicationAttribution> dblinkPubs = dblink.getPublications();
            if (dblinkPubs != null && !dblinkPubs.isEmpty()) {
                for (PublicationAttribution pubAttr : dblinkPubs) {
                    publications.add(pubAttr.getPublication());
                }
            }
        }

        return publications;
    }


    public static List<String> getCloneMarkerTypes() {
        // set clone marker types
        List<String> typeList = new ArrayList<>();
        typeList.add(Marker.Type.BAC.toString());
        typeList.add(Marker.Type.PAC.toString());
        typeList.add(Marker.Type.FOSMID.toString());

        typeList.add(Marker.Type.EST.toString());
        typeList.add(Marker.Type.CDNA.toString());
        return typeList;
    }

    public static List<String> getSuppliers(Marker marker) {
        Set<MarkerSupplier> markerSuppliers = marker.getSuppliers();
        List<String> supplierList = new ArrayList<>();
        for (MarkerSupplier markerSupplier : markerSuppliers) {
            supplierList.add(markerSupplier.getOrganization().getName());
        }
        return supplierList;
    }

    public static boolean markerHasSupplier(Marker marker, Organization supplier) {
        Collection<MarkerSupplier> suppliers = marker.getSuppliers();
        if (CollectionUtils.isNotEmpty(suppliers)) {
            for (MarkerSupplier markerSupplier : marker.getSuppliers()) {
                if (markerSupplier.getOrganization().equals(supplier)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> getDirectAttributions(Marker marker) {
        // get direct attributions
        ActiveData activeData = new ActiveData();
        activeData.setZdbID(marker.getZdbID());
        List<RecordAttribution> recordAttributions = RepositoryFactory.getInfrastructureRepository().getRecordAttributions(activeData);
        List<String> attributions = new ArrayList<>();
        for (RecordAttribution recordAttribution : recordAttributions) {
            attributions.add(recordAttribution.getSourceZdbID());
        }
        return attributions;
    }

    public static MutantOnMarkerBean getMutantsOnGene(Marker gene) {
        MutantOnMarkerBean mutantOnMarkerBean = new MutantOnMarkerBean();
        mutantOnMarkerBean.setGenotypeList(getMarkerRepository().getMutantsAndTgsByGene(gene.getZdbID()));
        mutantOnMarkerBean.setFeatures(getMutantRepository().getAllelesForMarker(gene.getZdbID(), "is allele of"));
        mutantOnMarkerBean.setKnockdownReagents(getMarkerRepository().getRelatedMarkerDisplayForTypes(gene, false, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE));

        return mutantOnMarkerBean;
    }


    public static PhenotypeOnMarkerBean getPhenotypeOnGene(Marker gene) {
        PhenotypeOnMarkerBean phenotypeOnMarkerBean = new PhenotypeOnMarkerBean();
        phenotypeOnMarkerBean.setNumFigures(RepositoryFactory.getPhenotypeRepository().getNumPhenotypeFigures(gene));
        phenotypeOnMarkerBean.setNumPublications(RepositoryFactory.getPhenotypeRepository().getNumPhenotypePublications(gene));
        phenotypeOnMarkerBean.setAnatomy(RepositoryFactory.getPhenotypeRepository().getPhenotypeAnatomy(gene));
        if (phenotypeOnMarkerBean.getNumPublications() == 1) {
            phenotypeOnMarkerBean.setSinglePublicationLink(RepositoryFactory.getPhenotypeRepository().getPhenotypeFirstPublication(gene));
        }
        if (phenotypeOnMarkerBean.getNumFigures() == 1) {
            phenotypeOnMarkerBean.setSingleFigureLink(RepositoryFactory.getPhenotypeRepository().getPhenotypeFirstFigure(gene));
        }

        return phenotypeOnMarkerBean;
    }

    public static GeneOntologyOnMarkerBean getGeneOntologyOnMarker(Marker gene) {
        GeneOntologyOnMarkerBean geneOntologyOnMarkerBean = new GeneOntologyOnMarkerBean();

        geneOntologyOnMarkerBean.setGoTermCount(
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getEvidenceForMarkerCount(gene));

        geneOntologyOnMarkerBean.setBiologicalProcessEvidence(
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getFirstEvidenceForMarkerOntology(gene, Ontology.GO_BP));
        geneOntologyOnMarkerBean.setCellularComponentEvidence(
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getFirstEvidenceForMarkerOntology(gene, Ontology.GO_CC));
        geneOntologyOnMarkerBean.setMolecularFunctionEvidence(
                RepositoryFactory.getMarkerGoTermEvidenceRepository().getFirstEvidenceForMarkerOntology(gene, Ontology.GO_MF));

        return geneOntologyOnMarkerBean;
    }

    /**
     * Retrieve presentation for a collection of orthologs for the same zebrafish gene
     *
     * @param orthologs
     * @param publication
     * @return
     */
    public static OrthologyPresentationBean getOrthologyPresentationBean(Collection<Ortholog> orthologs, Marker gene, Publication publication) {
        OrthologyPresentationBean orthologyPresentationBean = new OrthologyPresentationBean();
        if (CollectionUtils.isNotEmpty(orthologs)) {
            List<OrthologyPresentationRow> rows = new ArrayList<>();
            for (Ortholog ortholog : orthologs) {
                OrthologyPresentationRow row = new OrthologyPresentationRow();
                row.setOrthoID(ortholog.getZdbID());
                row.setSpecies(ortholog.getOrganism().getCommonName());
                row.setAbbreviation(ortholog.getSymbol());
                row.setChromosome(ortholog.getChromosome());
                row.setAccessions(ortholog.getExternalReferenceList());

                // Collect all the evidence records by code into a map then pull out the values
                Map<String, OrthologEvidencePresentation> evidenceMap = new TreeMap<>();
                for (OrthologEvidence evidence : ortholog.getEvidenceSet()) {
                    if (publication != null && !publication.equals(evidence.getPublication())) {
                        continue;
                    }
                    String key = evidence.getEvidenceCode().getName();
                    if (!evidenceMap.containsKey(key)) {
                        OrthologEvidencePresentation evidencePresentation = new OrthologEvidencePresentation();
                        evidencePresentation.setCode(evidence.getEvidenceCode());
                        evidenceMap.put(key, evidencePresentation);
                    }
                    evidenceMap.get(key).addPublication(evidence.getPublication());
                }
                // if the evidence map is empty (probably because a publication was provided, and that publication
                // does not support the current ortholog), then just go to the next ortholog; don't add a row with
                // no evidence.
                if (MapUtils.isEmpty(evidenceMap)) {
                    continue;
                }
                row.setEvidence(evidenceMap.values());
                rows.add(row);
            }
            orthologyPresentationBean.setOrthologs(rows);
        }
        OrthologyNote note = gene.getOrthologyNote();
        if (note != null) {
            orthologyPresentationBean.setNote(note.getNote());
        }

        return orthologyPresentationBean;
    }

    public static OrthologyPresentationBean getOrthologyEvidence(Marker gene, Publication publication) {
        Collection<Ortholog> orthologs = getOrthologyRepository().getOrthologs(gene);
        return getOrthologyPresentationBean(orthologs, gene, publication);
    }

    public static OrthologyPresentationBean getOrthologyEvidence(Marker gene) {
        return getOrthologyEvidence(gene, null);
    }

    public static String getMarkerTypeString(Marker marker) {
        return markerRepository.getMarkerTypeByName(marker.getType().name()).getDisplayName();
    }

    public static MarkerBean createDefaultViewForMarker(MarkerBean markerBean) {

        Marker marker = markerBean.getMarker();
        logger.debug("marker is:" + marker.getZdbID());
        String zdbID = marker.getZdbID();
        if (Marker.Type.GENE == marker.getType()) {
            List<OmimPhenotype> omimPhenotypes = markerRepository.getOmimPhenotype(marker);
            if (omimPhenotypes == null || omimPhenotypes.size() == 0) {
                markerBean.setDiseaseDisplays(null);
            } else {
                SortedSet<DiseaseDisplay> diseaseDisplays = getDiseaseDisplays(omimPhenotypes);
                List<DiseaseDisplay> diseaseDisplaysList = new ArrayList<>(diseaseDisplays.size());
                diseaseDisplaysList.addAll(diseaseDisplays);
                markerBean.setDiseaseDisplays(diseaseDisplaysList);
            }
        }

        markerBean.setMarkerTypeDisplay(getMarkerTypeString(marker));

        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(marker));

        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID));

        // OTHER GENE / MARKER PAGES:
        markerBean.setOtherMarkerPages(markerRepository.getMarkerDBLinksFast(marker, DisplayGroup.GroupName.SUMMARY_PAGE));


        // sequence info page
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(marker));

        // MARKER RELATIONSHIPS (same as clone relationships for gene)
        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));
        Collections.sort(cloneRelationships, markerRelationshipSupplierComparator);
        markerBean.setMarkerRelationshipPresentationList(cloneRelationships);

//      CITATIONS
        markerBean.setNumPubs(publicationRepository.getNumberAssociatedPublicationsForZdbID(marker.getZdbID()));

        return markerBean;
    }

    public static GeneBean pullClonesOntoGeneFromTranscript(GeneBean geneBean) {
        List<MarkerRelationshipPresentation> markerRelationshipPresentationList = geneBean.getMarkerRelationshipPresentationList();
        List<MarkerRelationshipPresentation> clonesForGeneTranscripts =
                markerRepository.getWeakReferenceMarker(geneBean.getMarker().getZdbID()
                        , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                        , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        for (MarkerRelationshipPresentation markerRelationshipPresentation : clonesForGeneTranscripts) {
            if (false == markerRelationshipPresentationList.contains(markerRelationshipPresentation)) {
                markerRelationshipPresentationList.add(markerRelationshipPresentation);
            }
        }
        Collections.sort(markerRelationshipPresentationList, markerRelationshipSupplierComparator);
        geneBean.setMarkerRelationshipPresentationList(markerRelationshipPresentationList);
        return geneBean;
    }

    public static CloneBean pullGeneOntoCloneFromTranscript(CloneBean cloneBean) {
        List<MarkerRelationshipPresentation> markerRelationshipPresentationList = cloneBean.getMarkerRelationshipPresentationList();
        List<MarkerRelationshipPresentation> genesForCloneTranscripts =
                markerRepository.getWeakReferenceMarker(cloneBean.getMarker().getZdbID()
                        , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                        , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                        , "Contains");
        for (MarkerRelationshipPresentation markerRelationshipPresentation : genesForCloneTranscripts) {
            if (false == markerRelationshipPresentationList.contains(markerRelationshipPresentation)) {
                markerRelationshipPresentationList.add(markerRelationshipPresentation);
            }
        }
        Collections.sort(markerRelationshipPresentationList, markerRelationshipSupplierComparator);
        cloneBean.setMarkerRelationshipPresentationList(markerRelationshipPresentationList);
        return cloneBean;
    }

    public static String getTypeForZdbID(String zdbID) {
        Matcher matcher = typePattern.matcher(zdbID);
        if (matcher.matches()) {
            int numGroups = matcher.groupCount();
            assert (numGroups == 1);
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Retrieve the accession number for Ensembl for a given ZFIN marker.
     *
     * @param marker
     * @return
     */
    public static String getEnsemblAccessionId(Marker marker) {
        Database.AvailableAbbrev database = Database.AvailableAbbrev.ENSEMBL_ZF;
        String accessionID = getMarkerRepository().getAccessionNumber(marker, database);
        return accessionID;
    }

    public static List<GenotypeFigure> getPhenotypeDataForSTR(SequenceTargetingReagent str) {
        return getMutantRepository().getGenotypeFiguresBySTR(str);
    }

    public static SortedSet<DiseaseDisplay> getDiseaseDisplays(List<OmimPhenotype> omimPhenotypes) {

        if (omimPhenotypes == null || omimPhenotypes.size() == 0) {
            return null;
        }

        DiseaseDisplay diseaseDisplay;
        SortedSet<DiseaseDisplay> diseaseDisplays = new TreeSet<>();
        for (OmimPhenotype omimPhenotype : omimPhenotypes) {
            Set<TermExternalReference> termExternalReferences = omimPhenotype.getExternalReferences();
            if (termExternalReferences != null && termExternalReferences.size() > 0) {
                for (TermExternalReference termExternalReference : termExternalReferences) {
                    diseaseDisplay = new DiseaseDisplay();
                    diseaseDisplay.setDiseaseTerm(termExternalReference.getTerm());
                    diseaseDisplay.setOmimTerm(omimPhenotype.getName());
                    diseaseDisplay.setOmimNumber(omimPhenotype.getOmimNum());
                    diseaseDisplays.add(diseaseDisplay);
                }
            } else {
                diseaseDisplay = new DiseaseDisplay();
                diseaseDisplay.setDiseaseTerm(null);
                diseaseDisplay.setOmimTerm(omimPhenotype.getName());
                diseaseDisplay.setOmimNumber(omimPhenotype.getOmimNum());
                diseaseDisplays.add(diseaseDisplay);
            }
        }

        return diseaseDisplays;
    }

    public static String getSTRModificationNote(String sequence, boolean reversed, boolean complemented) {
        String note = "Reported sequence " + sequence + " was";
        note += (reversed) ? " reversed" : "";
        note += (reversed && complemented) ? " and" : "";
        note += (complemented) ? " complemented" : "";
        note += ".";
        return note;
    }

}
