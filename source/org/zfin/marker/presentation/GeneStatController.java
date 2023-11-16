package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.stats.StatisticGeneService;
import org.zfin.stats.StatisticPublicationService;
import org.zfin.stats.StatisticRow;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/marker/stats")
public class GeneStatController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private HttpServletRequest request;


    @JsonView(View.API.class)
    @RequestMapping(value = "/transcript/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationDatasetsStats(@RequestParam(value = "filter.markerID", required = false) String publicationID,
                                                                        @Version Pagination pagination) {

        StatisticGeneService service = new StatisticGeneService();
        JsonResultResponse<StatisticRow> response = service.getTranscriptStats(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

}

