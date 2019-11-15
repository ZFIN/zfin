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
import org.zfin.sequence.Sequence;
import org.zfin.sequence.service.SequenceService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class SequenceController {

    public SequenceController() {
    }

    @Autowired
    private SequenceService sequenceService;

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
        Pagination pagination = new Pagination();
        if (limit != null)
            pagination.setLimit(limit);
        if (page != null)
            pagination.setPage(page);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_ACCESSION, accessionNumber);
        pagination.addFieldFilter(FieldFilter.SEQUENCE_TYPE, type);
        pagination.setSortBy(sortBy);

        JsonResultResponse<MarkerDBLink> response = sequenceService.getMarkerDBLinkJsonResultResponse(zdbID, pagination);
        response.setHttpServletRequest(request);

        return response;
    }

}