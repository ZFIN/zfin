package org.zfin.marker.service;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.*;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.AttributionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.mapping.repository.LinkageRepository;
import org.zfin.marker.*;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.presentation.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.GenotypeFigure;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.presentation.DiseaseModelDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermExternalReference;
import org.zfin.ontology.presentation.DiseaseDisplay;
import org.zfin.ontology.service.OntologyService;
import org.zfin.orthology.Ortholog;
import org.zfin.orthology.OrthologEvidence;
import org.zfin.orthology.OrthologExternalReference;
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
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.service.TranscriptService;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.sequence.ForeignDB.AvailableName.UNIPROTKB;

/**
 * Service Class that deals with Marker related logic.
 */
@Service
public class MarkerService {

    private static Logger logger = LogManager.getLogger(MarkerService.class);
    private static MarkerRepository markerRepository = getMarkerRepository();
    private static SequenceRepository sequenceRepository = getSequenceRepository();

    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();

    private static MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();

    private static Pattern typePattern = Pattern.compile("ZDB-([\\p{Alpha}_]+)-.*");

    private static Map<String, GenericTerm> soTermMapping;

    /**
     * Looks for firstMarkers in Genedom and returns the entire relation.
     *
     * @param marker Marker in firstMarkerRelation.
     * @return Returns set of marker relationships related by GENEDOM.
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

    public static List<MarkerDBLink> aggregateDBLinksByPub(Collection<MarkerDBLink> links) {
        return links.stream()
                .collect(
                        groupingBy(MarkerDBLink::getReferenceDatabaseForeignDB,
                        groupingBy(MarkerDBLink::getAccessionNumber))
                )
                .values()
                .stream()
                .flatMap(markerDBLinksMap ->
                    markerDBLinksMap.values().stream().map(MarkerService::consolidateMarkerDBLinks)
                )
                .collect(toList());
    }

    private static MarkerDBLink consolidateMarkerDBLinks(List<MarkerDBLink> markerDBLinks) {
        MarkerDBLink dbLink = markerDBLinks.get(0);
        MarkerDBLink link = new MarkerDBLink();
        link.setZdbID(dbLink.getZdbID());
        link.setAccessionNumber(dbLink.getAccessionNumber());
        link.setAccessionNumberDisplay(dbLink.getAccessionNumberDisplay());
        link.setLength(dbLink.getLength());
        link.setReferenceDatabase(dbLink.getReferenceDatabase());
        link.setPublications(dbLink.getPublications());
        link.setSequence(dbLink.getSequence());
        link.setLinkInfo(dbLink.getLinkInfo());
        markerDBLinks.forEach(markerDBLink -> link.addPublicationAttributions(markerDBLink.getPublications()));
        return link;
    }

    public static MarkerDBLink getMarkerDBLink(Marker marker, DBLink dbLink) {
        MarkerDBLink link = new MarkerDBLink();
        link.setMarker(marker);
        link.setZdbID(dbLink.getZdbID());
        link.setAccessionNumber(dbLink.getAccessionNumber());
        link.setAccessionNumberDisplay(dbLink.getAccessionNumberDisplay());
        link.setLength(dbLink.getLength());
        link.setReferenceDatabase(dbLink.getReferenceDatabase());
        link.setPublications(dbLink.getPublications());
        link.setLinkInfo(dbLink.getLinkInfo());
        return link;
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
            sequenceInfo.addDBLink(relatedLink.getLink());
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
     * Get links for a marker given a display group for context.
     * For example, ZDB-GENEP-161017-16 has related links to alliance, vega(OTTDARG00000044192), and ensembl (ENSDARG00000105749).
     *
     * @param marker
     * @param group
     * @return
     */
    public static List<LinkDisplay> getMarkerLinksForDisplayGroup(Marker marker, DisplayGroup.GroupName group, boolean addTranscriptDbLinks) {

        List<LinkDisplay> links = markerRepository.getMarkerDBLinksFast(marker, group);
        if ( group.equals(DisplayGroup.GroupName.OTHER_MARKER_PAGES) && addTranscriptDbLinks ) {
            //pull vega genes from transcript onto gene page (case 7586)
            links.addAll(markerRepository.getVegaGeneDBLinksTranscript(marker, DisplayGroup.GroupName.SUMMARY_PAGE));
        }
        return links;
    }

    /**
     * Alias for the above method, but accepting strings instead of objects.
     * @param markerId
     * @param groupName
     * @return
     */
    public static List<LinkDisplay> getMarkerLinksForDisplayGroup(String markerId, String groupName, boolean addTranscriptDbLinks) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        DisplayGroup.GroupName group = DisplayGroup.GroupName.getGroup(groupName);

        return getMarkerLinksForDisplayGroup(marker, group, addTranscriptDbLinks);
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

    public static Set<Marker> getRelatedMarkers(Marker marker, MarkerRelationshipType markerRelationshipType) {
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(markerRelationshipType.getName());
        return getRelatedMarker(marker, type);
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

    public static List<RelatedMarker> getRelatedMarkersOfAnyType(Marker marker) {
        List<RelatedMarker> relatedMarkers = new ArrayList<>();

        for (MarkerRelationship mrel : marker.getFirstMarkerRelationships()) {
            relatedMarkers.add(new RelatedMarker(marker, mrel));

        }
        for (MarkerRelationship mrel : marker.getSecondMarkerRelationships()) {
            relatedMarkers.add(new RelatedMarker(marker, mrel));
        }

        return relatedMarkers;
    }

    public static List<MarkerRelationshipPresentation> getRelatedMarkerDisplayExcludeType(Marker marker, final boolean is1to2) {
        return markerRepository.getRelatedMarkerOrderDisplayExcludeTypes(marker, is1to2
                , MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE
                , MarkerRelationship.Type.PROMOTER_OF
                , MarkerRelationship.Type.CODING_SEQUENCE_OF
                , MarkerRelationship.Type.CONTAINS_REGION
                , MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY
                , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                , MarkerRelationship.Type.TRANSCRIPT_TARGETS_GENE
                , MarkerRelationship.Type.CRISPR_TARGETS_REGION
                , MarkerRelationship.Type.TALEN_TARGETS_REGION
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

    public static MarkerRelationship addMarkerRelationship(Marker firstMarker,
                                                           Marker secondMarker,
                                                           Publication publication,
                                                           MarkerRelationshipType markerRelationshipType) {
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(markerRelationshipType.getName());
        return addMarkerRelationship(firstMarker, secondMarker, publication.getZdbID(), type);
    }

    public static DBLink addMarkerLinkByAccession(Marker marker, String accessionNo, ReferenceDatabase refDB,
                                                  List<String> referenceIDs, Integer length) throws InvalidWebRequestException {
        DBLink link = null;

        Collection<? extends DBLink> links = marker.getDbLinks();
        if (CollectionUtils.isNotEmpty(links)) {
            for (DBLink dbLink : marker.getDbLinks()) {
                if (dbLink.getReferenceDatabase().equals(refDB) && dbLink.getAccessionNumber().equals(accessionNo)) {
                    throw new InvalidWebRequestException("marker.link.duplicate");
                }
            }
        }

        Iterator<String> referenceIDsIterator = referenceIDs.iterator();
        String pubId = referenceIDsIterator.next();

        if (length == null) {
            link = markerRepository.addDBLink(marker, accessionNo, refDB, pubId);
        } else {
            link = markerRepository.addDBLinkWithLenth(marker, accessionNo, refDB, pubId, length);
        }

        while (referenceIDsIterator.hasNext()) {
            Publication publication = publicationRepository.getPublication(referenceIDsIterator.next());
            markerRepository.addDBLinkAttribution(link, publication, marker);
        }
        
        return link;
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
        //  MarkerRelationship markerRelationship = markerRepository.getMarkerRelationship(marker1, marker2, markerRelationshipType);
        MarkerRelationship markerRelationship = markerRepository.getMarkerRelationship(marker1, marker2, markerRelationshipType);

        //now deal with attribution
        if (pubZdbID != null && pubZdbID.length() > 0) {
            int deletedRecord = infrastructureRepository.deleteRecordAttribution(markerRelationship.getZdbID(), pubZdbID);
            logger.info("deleted record attrs: " + deletedRecord);
        }
    }

    public static void deleteMarkerRelationshipAttribution(Marker marker1, Marker marker2, String pubZdbID
    ) {
        //  MarkerRelationship markerRelationship = markerRepository.getMarkerRelationship(marker1, marker2, markerRelationshipType);
        MarkerRelationship markerRelationship = markerRepository.getMarkerRelationship(marker1, marker2);

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

    public static void deleteMarkerRelationship(Marker marker1, Marker marker2) {
        MarkerRelationship mrel = getMarkerRepository().getMarkerRelationship(marker1, marker2);
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

        Set<MarkerRelationship> allRelationships = new HashSet<>(marker.getFirstMarkerRelationships());
        allRelationships.addAll(marker.getSecondMarkerRelationships());

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
        MarkerSupplier result = getProfileRepository().getSpecificSupplier(marker, supplier);
        return result != null;
    }

    public static boolean markerHasAlias(Marker marker, String alias) {
        Collection<MarkerAlias> aliases = marker.getAliases();
        if (CollectionUtils.isNotEmpty(aliases)) {
            for (MarkerAlias markerAlias : aliases) {
                if (markerAlias.getAlias().equals(alias)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static MarkerAlias createMarkerAlias(Marker marker, String newAlias, List<String> publicationIDs) {
        boolean isFirstIteration = true;
        MarkerAlias alias = null;

        for(String pubID : publicationIDs){
            Publication publication = publicationRepository.getPublication(pubID);

            // alias is created with the first reference. others added after the alias is created.
            if (isFirstIteration) {
                alias = markerRepository.addMarkerAlias(marker, newAlias, publication);
                isFirstIteration = false;
            } else {
                markerRepository.addDataAliasAttribution(alias, publication, marker);
            }

            // add direct attribution to aliased marker and provided publications
            infrastructureRepository.insertStandardPubAttribution(marker.getZdbID(), publication);
        }

        return alias;
    }

    public static List<String> getDirectAttributions(Marker marker) {
        // get direct attributions
        return getDirectAttributions(marker.getZdbID());
    }

    public static List<String> getDirectAttributions(String markerZdbID) {
        // get direct attributions
        List<RecordAttribution> recordAttributions = getInfrastructureRepository().getRecordAttributions(markerZdbID);
        return recordAttributions.stream().map(RecordAttribution::getSourceZdbID).toList();
    }

    public static MutantOnMarkerBean getMutantsOnGene(Marker gene) {
        MutantOnMarkerBean mutantOnMarkerBean = new MutantOnMarkerBean();
        mutantOnMarkerBean.setGenotypeList(markerRepository.getMutantsAndTgsByGene(gene.getZdbID()));
        mutantOnMarkerBean.setFeatures(getMutantRepository().getAllelesForMarker(gene.getZdbID(), "is allele of"));
        MarkerRelationship.Type relationshipType = gene.isNontranscribed() ?
                MarkerRelationship.Type.CRISPR_TARGETS_REGION :
                MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE;
        List<Marker> knockdownReagents = markerRepository.getRelatedMarkersForTypes(gene, relationshipType);
        List<SequenceTargetingReagentBean> knockdownBeans = new ArrayList<>(knockdownReagents.size());
        for (Marker knockdownReagent : knockdownReagents) {
            SequenceTargetingReagentBean bean = new SequenceTargetingReagentBean();
            bean.setMarker(knockdownReagent);
            bean.setGenomicFeatures(markerRepository.getFeaturesBySTR(knockdownReagent));
            knockdownBeans.add(bean);
        }
        mutantOnMarkerBean.setKnockdownReagents(knockdownBeans);
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


    public static ProteinDetailDomainBean getProteinDomainDetailBean(Marker gene) {
        List<DBLink> protIDs = sequenceRepository.getDBLinksForMarker(gene.getZdbID(), ForeignDBDataType.SuperType.PROTEIN);
        protIDs.addAll(sequenceRepository.getDBLinksForMarker(gene.getZdbID(), ForeignDBDataType.SuperType.SEQUENCE));
        ProteinDetailDomainBean proteinDetailDomainBean = new ProteinDetailDomainBean();
        if (CollectionUtils.isNotEmpty(protIDs)) {
            List<ProteinDomainRow> rows = new ArrayList<>();

            for (DBLink prot : protIDs) {
                ProteinDomainRow row = new ProteinDomainRow();

                if (prot.getReferenceDatabase().getForeignDB().getDbName() == UNIPROTKB) {
                    row.setProDBLink(prot);

                    List<ProteinToPDB> ptp = markerRepository.getPDB(prot.getAccessionNumberDisplay());
                    if (CollectionUtils.isNotEmpty(ptp)) {
                        row.setPDB(true);
                    }
                    Map<String, String> detailMap = new TreeMap<>();
                    for (String uniqIpName : markerRepository.getProteinType(gene)) {
                        detailMap.put(uniqIpName, "");
                    }

                    for (String ipName : markerRepository.getIPNames(prot.getAccessionNumberDisplay())) {
                        detailMap.put(ipName, "X");
                    }

                    row.setInterProDomain(detailMap);
                    rows.add(row);
                }
            }
            proteinDetailDomainBean.setInterProDomains(rows);
        }
        return proteinDetailDomainBean;
    }


    public static OrthologyPresentationBean getOrthologyEvidence(Marker gene, Publication publication) {
        Collection<Ortholog> orthologs = getOrthologyRepository().getOrthologs(gene);
        if (orthologs != null) {
            return getOrthologyPresentationBean(orthologs, gene, publication);
        } else {
            return null;
        }
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
        if (marker.isGenedom()) {
            markerBean.setDiseaseDisplays(getDiseaseDisplays(marker));
            markerBean.setDiseaseModelDisplays(getDiseaseModelDisplays(marker));

            markerBean.setAllianceGeneDesc(markerRepository.getGeneDescByMkr(marker));
        }

        markerBean.setMarkerTypeDisplay(getMarkerTypeString(marker));
        markerBean.setZfinSoTerm(getSoTerm(marker));

        markerBean.setPreviousNames(markerRepository.getPreviousNamesLight(marker));

        markerBean.setHasMarkerHistory(markerRepository.getHasMarkerHistory(zdbID));

        // OTHER GENE / MARKER PAGES:
        markerBean.setOtherMarkerPages(getMarkerLinksForDisplayGroup(marker, DisplayGroup.GroupName.SUMMARY_PAGE, true));


        // sequence info page
        markerBean.setSequenceInfo(MarkerService.getSequenceInfoSummary(marker));

        // MARKER RELATIONSHIPS (same as clone relationships for gene)
        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));
        cloneRelationships.sort(markerRelationshipSupplierComparator);
        markerBean.setMarkerRelationshipPresentationList(cloneRelationships);

//      CITATIONS
        markerBean.setNumPubs(publicationRepository.getNumberAssociatedPublicationsForZdbID(marker.getZdbID()));

        return markerBean;
    }

    private static Collection<DiseaseModelDisplay> getDiseaseModelDisplays(Marker marker) {
        List<DiseaseAnnotationModel> diseaseAnnotationModels = RepositoryFactory.getPhenotypeRepository().getDiseaseAnnotationModelsByGene(marker);
        Collection<DiseaseModelDisplay> diseaseModelDisplay = OntologyService.getDiseaseModelDisplay(diseaseAnnotationModels);
        return diseaseModelDisplay;
    }

    private static List<DiseaseDisplay> getDiseaseDisplays(Marker marker) {
        List<OmimPhenotype> omimPhenotypes = markerRepository.getOmimPhenotype(marker);
        if (omimPhenotypes == null || omimPhenotypes.size() == 0) {
            return null;
        } else {
            Set<DiseaseDisplay> diseaseDisplays = getDiseaseDisplays(omimPhenotypes);
            List<DiseaseDisplay> diseaseDisplaysList = new ArrayList<>(diseaseDisplays.size());
            diseaseDisplaysList.addAll(diseaseDisplays);
            Collections.sort(diseaseDisplaysList);
            return diseaseDisplaysList;
        }
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
        markerRelationshipPresentationList.sort(markerRelationshipSupplierComparator);
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
        markerRelationshipPresentationList.sort(markerRelationshipSupplierComparator);
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
        return getMarkerRepository().getAccessionNumber(marker, database);
    }

    public static List<GenotypeFigure> getPhenotypeDataForSTR(SequenceTargetingReagent str) {
        return getMutantRepository().getGenotypeFiguresBySTR(str);
    }

    public static Set<DiseaseDisplay> getDiseaseDisplays(List<OmimPhenotype> omimPhenotypes) {

        if (omimPhenotypes == null || omimPhenotypes.size() == 0) {
            return null;
        }

        DiseaseDisplay diseaseDisplay;
        Set<DiseaseDisplay> diseaseDisplays = new HashSet<>();
        for (OmimPhenotype omimPhenotype : omimPhenotypes) {
            Set<TermExternalReference> termExternalReferences = omimPhenotype.getExternalReferences();
            if (termExternalReferences != null && termExternalReferences.size() > 0) {
                for (TermExternalReference termExternalReference : termExternalReferences) {
                    diseaseDisplay = new DiseaseDisplay();
                    diseaseDisplay.setDiseaseTerm(termExternalReference.getTerm());
                    diseaseDisplay.setOmimPhenotype(omimPhenotype);
                    diseaseDisplays.add(diseaseDisplay);
                }
            } else {
                diseaseDisplay = new DiseaseDisplay();
                diseaseDisplay.setDiseaseTerm(null);
                diseaseDisplay.setOmimPhenotype(omimPhenotype);
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

    public static GenericTerm getSoTerm(Marker marker) {
        if (soTermMapping == null) {
            soTermMapping = getMarkerRepository().getSoTermMapping();
        }
        return soTermMapping.get(marker.getMarkerType().getName());
    }

    public String getActiveMarkerID(String zdbID) throws MarkerNotFoundException {
        if (zdbID.startsWith("ZDB-")) {
            if (markerRepository.markerExistsForZdbID(zdbID)) {
                return zdbID;
            }

            String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);

            logger.debug("trying to find a replaced zdbID for: " + zdbID);
            if (replacedZdbID != null) {
                logger.debug("found a replaced zdbID for: " + zdbID + "->" + replacedZdbID);
                return replacedZdbID;
            }
        } else {
            Marker marker = markerRepository.getMarkerByAbbreviationIgnoreCase(zdbID);

            if (marker == null) {
                marker = markerRepository.getMarkerByName(zdbID);
            }

            if (marker == null) {
                List<Marker> markers = markerRepository.getMarkersByAlias(zdbID);
                if (markers != null && markers.size() == 1) {
                    marker = markers.get(0);
                }
            }

            if (marker != null) {
                return marker.getZdbID();
            }
        }

        // if we got to this point we could not resolve the ID by any means, so bail out
        throw new MarkerNotFoundException(zdbID);
    }


    /**
     * All types except GENEFAMILY and GENEP are treated like genes for the controller
     */
    public boolean isOfTypeGene(String zdbID) {
        MarkerTypeGroup group = markerRepository.getMarkerTypeGroupByName(Marker.TypeGroup.GENEDOM.toString());

        Marker marker = markerRepository.getMarkerByID(zdbID);
        if (!group.hasType(marker.getType())) {
            return false;
        }
        if (marker.getType().equals(Marker.Type.GENEP) || marker.getType().equals(Marker.Type.GENEFAMILY)) {
            return false;
        }
        return true;
    }

    public static boolean isOfTypeClone(String zdbID) {
        MarkerTypeGroup group = markerRepository.getMarkerTypeGroupByName(Marker.TypeGroup.CLONEDOM.toString());
        Marker marker = markerRepository.getMarkerByID(zdbID);
        return group.hasType(marker.getType());
    }

    public JsonResultResponse<MarkerRelationshipPresentation> getMarkerRelationshipJsonResultResponse(String zdbID,
                                                                                                      Pagination pagination) {
        long startTime = System.currentTimeMillis();
        Marker marker = markerRepository.getMarker(zdbID);
        // needs refactor to remove hardcoding of fdbcont ids
        ReferenceDatabase genbankRNA = getSequenceRepository().getReferenceDatabaseByID("ZDB-FDBCONT-040412-37");
        ReferenceDatabase genbankGenomic = getSequenceRepository().getReferenceDatabaseByID("ZDB-FDBCONT-040412-36");

        if (marker == null) {
            String errorMessage = "No marker found for ID: " + zdbID;
            logger.error(errorMessage);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(errorMessage);
            throw new RestErrorException(error);
        }

        List<MarkerRelationshipPresentation> fullMarkerRelationships = new ArrayList<>();
        fullMarkerRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, true));
        fullMarkerRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));

        List<MarkerRelationshipPresentation> clonesForGeneTranscripts =
                markerRepository.getWeakReferenceMarker(marker.getZdbID()
                        , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                        , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT);
        for (MarkerRelationshipPresentation markerRelationshipPresentation : clonesForGeneTranscripts) {
            if (!fullMarkerRelationships.contains(markerRelationshipPresentation)) {
                fullMarkerRelationships.add(markerRelationshipPresentation);
            }
        }
        List<MarkerRelationshipPresentation> genesForCloneTranscripts =
                markerRepository.getWeakReferenceMarker(marker.getZdbID()
                        , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
                        , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                        , "Contains");
        for (MarkerRelationshipPresentation markerRelationshipPresentation : genesForCloneTranscripts) {
            if (!fullMarkerRelationships.contains(markerRelationshipPresentation)) {
                fullMarkerRelationships.add(markerRelationshipPresentation);
            }
        }

        for (MarkerRelationshipPresentation mrelP : fullMarkerRelationships) {
            Marker relatedMarker = markerRepository.getMarker(mrelP.getZdbId());
            List<MarkerDBLink> mdbLink = sequenceRepository.getDBLinksForMarker(relatedMarker, genbankGenomic, genbankRNA);
            mrelP.setOtherMarkerGenBankDBLink(mdbLink);
            mrelP.setNumberOfPublications(RepositoryFactory.getPublicationRepository().getNumberAssociatedPublicationsForZdbID(mrelP.getMarkerRelationshipZdbId()));
            mrelP.setRelatedMarker(relatedMarker);
            List<Publication> publications = new ArrayList<>();
            List<PublicationAttribution> publicationAttributions = RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(mrelP.getMarkerRelationshipZdbId());
            for (PublicationAttribution pub : publicationAttributions) {
                publications.add(pub.getPublication());
            }
            if (publications.size() == 1) {
                mrelP.setSinglePublication(publications.iterator().next());
            }


        }

        fullMarkerRelationships.sort(markerRelationshipSupplierComparator);

        // filtering
        FilterService<MarkerRelationshipPresentation> filterService = new FilterService<>(new MarkerRelationshipFiltering());
        List<MarkerRelationshipPresentation> filteredMarkerRelationshipList = filterService.filterAnnotations(fullMarkerRelationships, pagination.getFieldFilterValueMap());

        // sorting
        MarkerRelationshipSorting sorting = new MarkerRelationshipSorting();
        filteredMarkerRelationshipList.sort(sorting.getComparator(pagination.getSortBy()));


        JsonResultResponse<MarkerRelationshipPresentation> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startTime);
        response.setTotal(filteredMarkerRelationshipList.size());

        // paginating
        response.setResults(filteredMarkerRelationshipList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }


    public List<String> getBeeGeeStrings(List<LinkDisplay> otherMarkerDBLinksLinks, List<Ortholog> orthologList) {
        List<String> bGeeIds = orthologList.stream()
                .map(ortholog -> ortholog.getExternalReferenceList().stream()
                        .filter(reference -> reference.getAccessionNumber().startsWith("ENS"))
                        .map(OrthologExternalReference::getAccessionNumber)
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        final Optional<LinkDisplay> ensdarg = otherMarkerDBLinksLinks.stream().filter(linkDisplay -> linkDisplay.getAccession().startsWith("ENSDARG"))
                .findFirst();
        if (ensdarg.isPresent()) {
            bGeeIds.add(ensdarg.get().getAccession());
            return bGeeIds;
        }
        return null;
    }

    public String getGeneTreeEnsdarg(Marker gene) {

        List<LinkDisplay> dbLinks = markerRepository.getMarkerDBLinksFast(gene, DisplayGroup.GroupName.SUMMARY_PAGE);
        Predicate<LinkDisplay> byEnsdarg = ensdarg -> ensdarg.getAccNumDisplay().startsWith("ENSDARG");
        List<LinkDisplay> ensdargLinks = dbLinks.stream().filter(byEnsdarg)
                .collect(Collectors.toList());
        for (LinkDisplay ensdarg : ensdargLinks) {
            for (PublicationAttribution pubAttr : infrastructureRepository.getPublicationAttributions(ensdarg.getDblinkZdbID())) {
                if (pubAttr.getPublication().getZdbID().contains("ZDB-PUB-061101-1")) {
                    return ensdarg.getAccNumDisplay();
                }
                if (pubAttr.getPublication().getZdbID().contains("ZDB-PUB-190221-12")) {
                    return ensdarg.getAccNumDisplay();
                }

            }
        }


        return null;
    }


    public JsonResultResponse<SequenceTargetingReagentBean> getSTRJsonResultResponse(String zdbID, Pagination pagination) {
        JsonResultResponse<SequenceTargetingReagentBean> response = new JsonResultResponse<>();
        List<SequenceTargetingReagentBean> list = getMutantsOnGene(markerRepository.getMarker(zdbID)).getKnockdownReagents();
        if (list == null) {
            return response;
        }
        response.setResults(list);
        response.setTotal(list.size());

        // sorting
        if (pagination.getSortBy() != null) {
            STRBeanSorting sorting = new STRBeanSorting();
            list.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(list.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public JsonResultResponse<Feature> getFeatureJsonResultResponse(String zdbID, Pagination pagination) {
        JsonResultResponse<Feature> response = new JsonResultResponse<>();
        List<Feature> list = getMutantsOnGene(markerRepository.getMarker(zdbID)).getFeatures();
        if (list == null) {
            return response;
        }
        response.setResults(list);
        response.setTotal(list.size());

        // filtering
/*
        FilterService<MarkerRelationshipPresentation> filterService = new FilterService<>(new MarkerRelationshipFiltering());
        List<MarkerRelationshipPresentation> filteredMarkerRelationshipList = filterService.filterAnnotations(fullMarkerRelationships, pagination.getFieldFilterValueMap());
*/

        // sorting
        if (pagination.getSortBy() != null) {
            FeatureSorting sorting = new FeatureSorting();
            list.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(list.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public JsonResultResponse<FluorescentProtein> getFPBaseJsonResultResponse(Pagination pagination) {
        JsonResultResponse<FluorescentProtein> response = new JsonResultResponse<>();
        List<FluorescentProtein> proteins = getMarkerRepository().getAllFluorescentProteins();
        if (proteins == null) {
            return response;
        }
        response.setResults(proteins);
        response.setTotal(proteins.size());

        // filtering
/*
        FilterService<MarkerRelationshipPresentation> filterService = new FilterService<>(new MarkerRelationshipFiltering());
        List<MarkerRelationshipPresentation> filteredMarkerRelationshipList = filterService.filterAnnotations(fullMarkerRelationships, pagination.getFieldFilterValueMap());
*/

        // sorting
        if (pagination.getSortBy() != null) {
            FluorescentProteinSorting sorting = new FluorescentProteinSorting();
            proteins.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(proteins.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public JsonResultResponse<FluorescentMarker> getFfgFluorescenceJsonResultResponse(Pagination pagination) {
        JsonResultResponse<FluorescentMarker> response = new JsonResultResponse<>();
        List<FluorescentMarker> efgs = getMarkerRepository().getAllFluorescentEfgs();
        if (efgs == null) {
            return response;
        }
        response.setResults(efgs);
        response.setTotal(efgs.size());

        // filtering
/*
        FilterService<MarkerRelationshipPresentation> filterService = new FilterService<>(new MarkerRelationshipFiltering());
        List<MarkerRelationshipPresentation> filteredMarkerRelationshipList = filterService.filterAnnotations(fullMarkerRelationships, pagination.getFieldFilterValueMap());
*/

        // sorting
        if (pagination.getSortBy() != null) {
            FluorescentMarkerSorting sorting = new FluorescentMarkerSorting();
            efgs.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(efgs.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public JsonResultResponse<FluorescentMarker> getConstructFluorescenceJsonResultResponse(Pagination pagination) {
        JsonResultResponse<FluorescentMarker> response = new JsonResultResponse<>();
        List<FluorescentMarker> efgs = getMarkerRepository().getAllFluorescentConstructs();
        if (efgs == null) {
            return response;
        }
        response.setResults(efgs);
        response.setTotal(efgs.size());

        // filtering
/*
        FilterService<MarkerRelationshipPresentation> filterService = new FilterService<>(new MarkerRelationshipFiltering());
        List<MarkerRelationshipPresentation> filteredMarkerRelationshipList = filterService.filterAnnotations(fullMarkerRelationships, pagination.getFieldFilterValueMap());
*/

        // sorting
        if (pagination.getSortBy() != null) {
            FluorescentMarkerSorting sorting = new FluorescentMarkerSorting();
            efgs.sort(sorting.getComparator(pagination.getSortBy()));
        }

        response.setResults(efgs.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

    public List<MarkerRelationshipEditMetadata> getMarkerRelationshipEditMetadata(Marker marker,
                                                                                  MarkerRelationship.Type... types) {
        return Arrays.stream(types)
                .map(typeEnum -> {
                    MarkerRelationshipType type = markerRepository.getMarkerRelationshipType(typeEnum.toString());
                    Marker.Type markerType = marker.getType();
                    MarkerRelationshipEditMetadata metadata = new MarkerRelationshipEditMetadata();
                    metadata.setType(type.getName());
                    if (type.getFirstMarkerTypeGroup().hasType(markerType)) {
                        metadata.set1to2(true);
                        metadata.setRelatedMarkerTypeGroup(type.getSecondMarkerTypeGroup().getName());
                        return metadata;
                    }
                    if (type.getSecondMarkerTypeGroup().hasType(markerType)) {
                        metadata.set1to2(false);
                        metadata.setRelatedMarkerTypeGroup(type.getFirstMarkerTypeGroup().getName());
                        return metadata;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }
}



