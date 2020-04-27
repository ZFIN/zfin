package org.zfin.sequence.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.Species;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.SequenceInfo;
import org.zfin.marker.presentation.SummaryDBLinkDisplay;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
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
                                                                              boolean summary) {
        long startTime = System.currentTimeMillis();
        Marker marker = markerRepository.getMarker(zdbID);
        if (marker == null) {
            String errorMessage = "No marker found for ID: " + zdbID;
            log.error(errorMessage);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(errorMessage);
            throw new RestErrorException(error);
        }

        JsonResultResponse<MarkerDBLink> response = new JsonResultResponse<>();
        if (summary) {
            SequenceInfo sequenceInfo = MarkerService.getSequenceInfoSummary(marker);
            response.setResults(sequenceInfo.getDbLinks().stream()
                    .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                    .collect(Collectors.toList())
            );
            response.setTotal(sequenceInfo.getNumberDBLinks());
        } else {
            List<MarkerDBLink> fullMarkerDBLinks = MarkerService.getMarkerDBLinks(marker);

            // filtering
            FilterService<MarkerDBLink> filterService = new FilterService<>(new SequenceFiltering());
            List<MarkerDBLink> filteredDBLinksList = filterService.filterAnnotations(fullMarkerDBLinks, pagination.getFieldFilterValueMap());

            // sorting
            SequenceSorting sorting = new SequenceSorting();
            filteredDBLinksList.sort(sorting.getComparator(pagination.getSortBy()));


            response.calculateRequestDuration(startTime);
            response.setTotal(filteredDBLinksList.size());

            // paginating
            response.setResults(filteredDBLinksList.stream()
                    .skip(pagination.getStart())
                    .limit(pagination.getLimit())
                    .collect(Collectors.toList()));
        }

        return response;
    }



    public JsonResultResponse<MarkerDBLink> getOtherMarkerDBLinkJsonResultResponse(String zdbID
                                                                              ) {
        Marker marker = markerRepository.getMarker(zdbID);
        if (marker == null) {
            String errorMessage = "No marker found for ID: " + zdbID;
            log.error(errorMessage);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(errorMessage);
            throw new RestErrorException(error);
        }

        JsonResultResponse<MarkerDBLink> response = new JsonResultResponse<>();

            SequenceInfo sequenceInfo = MarkerService.getMarkerSequenceInfo(marker);
            response.setResults(sequenceInfo.getDbLinks().stream()
                    .map(dbLink -> MarkerService.getMarkerDBLink(marker, dbLink))
                    .collect(Collectors.toList())
            );
            response.setTotal(sequenceInfo.getNumberDBLinks());

        return response;
    }



}
