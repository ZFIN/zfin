package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.*;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.sequence.MarkerDBLink;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class SequenceController {
    public SequenceController() {
        String name = "";
    }

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.SequenceAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/sequences")
    public JsonResultResponse<MarkerDBLink> getSequenceView(@PathVariable("zdbID") String zdbID,
                                                            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit,
                                                            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                                            @RequestParam(value = "sortBy", required = false) String sortBy,
                                                            @RequestParam(value = "filter.type", required = false) String type,
                                                            @RequestParam(value = "filter.accession", required = false) String accessionNumber,
                                                            @RequestParam(value = "filter.length", required = false) String length) {
        long startTime = System.currentTimeMillis();
        Pagination pagination = new Pagination();
        if (limit != null)
            pagination.setLimit(limit);
        if (page != null)
            pagination.setPage(page);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_ACCESSION, accessionNumber);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_TYPE, type);


        Marker marker = markerRepository.getMarker(zdbID);

        if (marker == null) {
            final String errorMessage = "No marker found for ID: " + zdbID;
            log.error(errorMessage);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(errorMessage);
            throw new RestErrorException(error);
        }

        List<MarkerDBLink> markerDBLinks = MarkerService.getMarkerDBLinks(marker);


        // filtering
        FilterService<MarkerDBLink> filterService = new FilterService<>(new SequenceFiltering());
        List<MarkerDBLink> filteredDBLinksList = filterService.filterAnnotations(markerDBLinks, pagination.getFieldFilterValueMap());

        // sorting
        SequenceSorting sorting = new SequenceSorting();
        filteredDBLinksList.sort(sorting.getComparator(sortBy));


        JsonResultResponse<MarkerDBLink> response = new JsonResultResponse<>();
        response.calculateRequestDuration(startTime);
        response.setResults(filteredDBLinksList);
        response.setTotal(filteredDBLinksList.size());
        response.setHttpServletRequest(request);

        // paginating
        response.setResults(filteredDBLinksList.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList()));
        return response;
    }

}