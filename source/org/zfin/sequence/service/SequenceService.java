package org.zfin.sequence.service;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Transcript;
import org.zfin.marker.presentation.DbLinkDisplayComparator;
import org.zfin.marker.presentation.RelatedMarkerDBLinkDisplay;
import org.zfin.marker.presentation.SequenceInfo;
import org.zfin.marker.presentation.SummaryDBLinkDisplay;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.blast.MountedWublastBlastService;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
@Log4j2
public class SequenceService {

    private final static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    @Autowired
    private MarkerRepository markerRepository;


    private ReferenceDatabase omimHumanOrthologDB;
    private ReferenceDatabase entrezGeneHumarnRefDB;
    private ReferenceDatabase entrezGeneMouseRefDB;
    private ReferenceDatabase uniprotDB;

    public ReferenceDatabase getOMIMHumanOrtholog() {
        if (omimHumanOrthologDB == null) {
            omimHumanOrthologDB = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.OMIM,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.HUMAN);
        }
        return omimHumanOrthologDB;
    }

    public ReferenceDatabase getEntrezGeneHumanRefDB() {
        if (entrezGeneHumarnRefDB == null) {
            entrezGeneHumarnRefDB = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.HUMAN);
        }

        return entrezGeneHumarnRefDB;
    }

    public ReferenceDatabase getEntrezGeneMouseRefDB() {
        if (entrezGeneMouseRefDB == null) {
            entrezGeneMouseRefDB = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENE,
                ForeignDBDataType.DataType.ORTHOLOG,
                ForeignDBDataType.SuperType.ORTHOLOG,
                Species.Type.MOUSE);
        }

        return entrezGeneMouseRefDB;
    }

    public ReferenceDatabase getUniprotDb() {
        if (uniprotDB == null) {
            uniprotDB = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.UNIPROTKB,
                ForeignDBDataType.DataType.POLYPEPTIDE,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);
        }

        return uniprotDB;
    }

    public static ReferenceDatabase getUniprotRefDB() {
        return sequenceRepository.getZebrafishSequenceReferenceDatabase(
            ForeignDB.AvailableName.UNIPROTKB
            , ForeignDBDataType.DataType.POLYPEPTIDE
        );
    }

    public static SummaryDBLinkDisplay getProteinProducts(Marker gene) {
        List<DBLink> dbLinkList = sequenceRepository.getDBLinksForMarker(gene.getZdbID(), ForeignDBDataType.SuperType.PROTEIN);
        SummaryDBLinkDisplay summaryDBLinkDisplay = new SummaryDBLinkDisplay();
        summaryDBLinkDisplay.addAllDBlinks(dbLinkList);
        return summaryDBLinkDisplay;
    }


    public JsonResultResponse<MarkerDBLink> getMarkerDBLinkJsonResultResponse(String zdbID,
                                                                              Pagination pagination,
                                                                              boolean summary,
                                                                              boolean getAllRecords) {
        long startTime = System.currentTimeMillis();

        JsonResultResponse<MarkerDBLink> response = new JsonResultResponse<>();
        List<MarkerDBLink> displayedLinks = getMarkerDBLinkResultsForMarkerZdbID(zdbID, summary, response, getAllRecords);
        response.addSupplementalData("superTypes", displayedLinks.stream()
            .map(markerDBLink -> markerDBLink.getReferenceDatabase().getForeignDBDataType().getSuperType().getValue()).distinct().toList());
        response.addSupplementalData("foreignDB", displayedLinks.stream()
            .map(markerDBLink -> markerDBLink.getReferenceDatabase().getForeignDB().getDbName().getValue()).distinct().toList());
        response.addSupplementalData("type", displayedLinks.stream()
            .map(DBLink::getSequenceType).distinct().toList());
        response.addSupplementalData("displayGroup", displayedLinks.stream()
            .map(markerDBLink -> markerDBLink.getReferenceDatabase().getDisplayGroups())
            .flatMap(Collection::stream)
            .map(displayGroup -> displayGroup.getGroupName().getValue())
            .distinct().toList());

        // filtering
        FilterService<MarkerDBLink> filterService = new FilterService<>(new SequenceFiltering());
        List<MarkerDBLink> filteredDBLinksList = filterService.filterAnnotations(displayedLinks, pagination.getFieldFilterValueMap());

        response.calculateRequestDuration(startTime);
        response.setTotal(filteredDBLinksList.size());

        // paginating
        response.setResults(filteredDBLinksList.stream()
            .skip(pagination.getStart())
            .limit(pagination.getLimit())
            .collect(Collectors.toList()));

        return response;
    }

    public List<MarkerDBLink> getMarkerDBLinkResultsForMarkerZdbID(String markerZdbID, boolean summary, JsonResultResponse<MarkerDBLink> response, boolean getAllRecords) {
        return getMarkerDBLinkResultsForMarkerZdbID(markerZdbID, summary, response, getAllRecords, false);
    }

    public List<MarkerDBLink> getMarkerDBLinkResultsForMarkerZdbID(String markerZdbID, boolean summary, JsonResultResponse<MarkerDBLink> response, boolean getAllRecords, boolean skipBlastRetrieval) {
        Marker marker = markerRepository.getMarker(markerZdbID);
        if (marker == null) {
            String errorMessage = "No marker found for ID: " + markerZdbID;
            log.error(errorMessage);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(errorMessage);
            throw new RestErrorException(error);
        }
        if(getAllRecords){
            return getAllMarkerDBLinkResultsForMarker(marker, summary, response);
        }
        return getMarkerDBLinkResultsForMarker(marker, summary, response, skipBlastRetrieval);
    }

    public List<Pair<String, String>> getMarkerRNAMapForNCBILoad() {
        return sequenceRepository.getAllRNADBLinksForAllMarkersInGenedom();
    }

    public List<MarkerDBLink> getMarkerDBLinkResultsForMarker(Marker marker, boolean summary, JsonResultResponse<MarkerDBLink> response, boolean skipBlastRetrieval) {
        List<MarkerDBLink> allDBLinks = new ArrayList<>();

        if (marker.getType().equals(Marker.Type.TSCRIPT)) {
            SequenceInfo supportingSequenceInfo = TranscriptService.getSupportingSequenceInfo((Transcript) marker);
            if (CollectionUtils.isNotEmpty(supportingSequenceInfo.getDbLinks())) {
                allDBLinks.addAll(supportingSequenceInfo
                    .getDbLinks()
                    .stream()
                    .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                    .toList()
                );
            }
        } else {
            List<MarkerDBLink> dbLinks = sequenceRepository
                    .getDBLinksForMarker(marker.getZdbID(), ForeignDBDataType.SuperType.SEQUENCE)
                    .stream()
                    .filter(dbLink -> !dbLink.getReferenceDatabase().getForeignDB().isFishMiRNAExpression())
                    .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                    .toList();
            allDBLinks.addAll(dbLinks);
            if (!skipBlastRetrieval) {
                // populate FishMiRNA sequence info
                populateFishMiRNASequenceInfo(dbLinks);
            }
        }

        if (marker.isGenedom()) {
            List<RelatedMarkerDBLinkDisplay> relatedLinks = RepositoryFactory.getSequenceRepository()
                .getDBLinksForFirstRelatedMarker(
                    marker,
                    DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                    MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                    MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
                );
            relatedLinks.addAll(RepositoryFactory.getSequenceRepository()
                .getDBLinksForSecondRelatedMarker(
                    marker,
                    DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                    MarkerRelationship.Type.CLONE_CONTAINS_GENE
                )
            );
            relatedLinks.addAll(MarkerService.getTranscriptReferences(marker));
            allDBLinks.addAll(relatedLinks.stream()
                .map(RelatedMarkerDBLinkDisplay::getLink)
                .collect(Collectors.toList())
            );
        }

        List<MarkerDBLink> groupedLinks = MarkerService.aggregateDBLinksByPub(allDBLinks);

        // links must be sorted before going into the summarizer so that the correct first one gets kept
        groupedLinks.sort(new DbLinkDisplayComparator());
        List<MarkerDBLink> displayedLinksOverview = new ArrayList<>();
        Set<ForeignDBDataType.DataType> types = new HashSet<>();
        for (MarkerDBLink link : groupedLinks) {
            ForeignDBDataType.DataType linkType = link.getReferenceDatabase().getForeignDBDataType().getDataType();
            if (!types.contains(linkType)) {
                displayedLinksOverview.add(link);
                types.add(linkType);
            }
        }

        if (response != null) {
            response.addSupplementalData("countDirect", displayedLinksOverview.size());
            response.addSupplementalData("countIncludingChildren", groupedLinks.size());
        }

        List<MarkerDBLink> displayedLinks;
        if (summary) {
            displayedLinks = displayedLinksOverview;
        } else {
            displayedLinks = groupedLinks;
        }
        return displayedLinks;
    }

    private static void populateFishMiRNASequenceInfo(List<MarkerDBLink> dbLinks) {
        // populate FishMiRNA sequence info
        dbLinks.stream().filter(markerDBLink -> markerDBLink.getReferenceDatabase().getForeignDB().isFishMiRNA())
            .forEach(fishMiRnaDBLink -> {
                List<Sequence> sequences = MountedWublastBlastService.getInstance().
                    getSequencesFromSource(fishMiRnaDBLink);
                if (CollectionUtils.isNotEmpty(sequences)) {
                    fishMiRnaDBLink.setSequence(sequences.get(0));
                }
            });
    }

    public List<MarkerDBLink> getAllMarkerDBLinkResultsForMarker(Marker marker, boolean summary, JsonResultResponse<MarkerDBLink> response) {
        List<MarkerDBLink> allDBLinks = new ArrayList<>();

        if (marker.getType().equals(Marker.Type.TSCRIPT)) {
            Transcript transcript = (Transcript) marker;
            if (CollectionUtils.isNotEmpty(transcript.getTranscriptDBLinks())) {
                allDBLinks.addAll(transcript.getTranscriptDBLinks()
                    .stream()
                    .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                    .toList()
                );
            }
        } else {
            List<MarkerDBLink> dbLinks = sequenceRepository
                .getDBLinksForMarker(marker.getZdbID(), ForeignDBDataType.SuperType.SEQUENCE)
                .stream()
                .filter(dbLink -> !dbLink.getReferenceDatabase().getForeignDB().isFishMiRNAExpression())
                .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                .toList();
            allDBLinks.addAll(dbLinks);
            // populate FishMiRNA sequence info
            populateFishMiRNASequenceInfo(dbLinks);
        }

        if (marker.isGenedom()) {
            List<RelatedMarkerDBLinkDisplay> relatedLinks = RepositoryFactory.getSequenceRepository()
                .getDBLinksForFirstRelatedMarker(
                    marker,
                    DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                    MarkerRelationship.Type.GENE_CONTAINS_SMALL_SEGMENT,
                    MarkerRelationship.Type.CLONE_CONTAINS_SMALL_SEGMENT,
                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT
                );
            relatedLinks.addAll(RepositoryFactory.getSequenceRepository()
                .getDBLinksForSecondRelatedMarker(
                    marker,
                    DisplayGroup.GroupName.MARKER_LINKED_SEQUENCE,
                    MarkerRelationship.Type.CLONE_CONTAINS_GENE
                )
            );
            relatedLinks.addAll(MarkerService.getTranscriptReferences(marker));
            allDBLinks.addAll(relatedLinks.stream()
                .map(RelatedMarkerDBLinkDisplay::getLink)
                .collect(Collectors.toList())
            );
        }

        List<MarkerDBLink> groupedLinks = MarkerService.aggregateDBLinksByPub(allDBLinks);

        // links must be sorted before going into the summarizer so that the correct first one gets kept
        groupedLinks.sort(new DbLinkDisplayComparator());
        List<MarkerDBLink> displayedLinksOverview = new ArrayList<>();
        Set<ForeignDBDataType.DataType> types = new HashSet<>();
        for (MarkerDBLink link : groupedLinks) {
            ForeignDBDataType.DataType linkType = link.getReferenceDatabase().getForeignDBDataType().getDataType();
            if (!types.contains(linkType)) {
                displayedLinksOverview.add(link);
                types.add(linkType);
            }
        }

        if (response != null) {
            response.addSupplementalData("countDirect", displayedLinksOverview.size());
            response.addSupplementalData("countIncludingChildren", groupedLinks.size());
        }

        List<MarkerDBLink> displayedLinks;
        if (summary) {
            displayedLinks = displayedLinksOverview;
        } else {
            displayedLinks = groupedLinks;
        }
        return displayedLinks;
    }

    public void setMarkerRepository(MarkerRepository markerRepository) {
        this.markerRepository = markerRepository;
    }
}
