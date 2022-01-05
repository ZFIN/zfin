package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.stats.StatisticPublicationService;
import org.zfin.stats.StatisticRow;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/publication/stats")
public class PublicationStatController {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.API.class)
    @RequestMapping(value = "/marker/{type}", method = RequestMethod.GET)
    public JsonResultResponse<PublicationStat> getPublicationMutation(@PathVariable String type,
                                                                      @Version Pagination pagination) {

        List<String> markerType = null;
        if (type.equals("marker"))
            markerType = List.of(Marker.TypeGroup.GENEDOM.name());
        if (type.equals("antibody"))
            markerType = List.of(Marker.TypeGroup.ATB.name());
        if (type.equals("str")) {
            markerType = List.of(Marker.TypeGroup.MRPHLNO.name(),
                    Marker.TypeGroup.TALEN.name(),
                    Marker.TypeGroup.CRISPR.name());
        }
        Map<Publication, Integer> histogramPubMarker = publicationRepository.getPublicationMarkerHistogram(markerType, pagination);
        List<PublicationStat> stats = histogramPubMarker.entrySet().stream()
                .map(entry -> {
                    PublicationStat stat = new PublicationStat();
                    stat.setMarkerCount(entry.getValue());
                    stat.setPublication(entry.getKey());
                    return stat;
                }).collect(Collectors.toList());

        JsonResultResponse<PublicationStat> response = new JsonResultResponse<>();
        response.setTotal(stats.size());
        List<PublicationStat> paginatedList = stats.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());

        response.setResults(paginatedList);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping(value = "/antibody/histogram", method = RequestMethod.GET)
    public JsonResultResponse<StatisticRow> getPublicationStats(@Version Pagination pagination) {

        StatisticPublicationService service = new StatisticPublicationService();
        JsonResultResponse<StatisticRow> response = service.getAllPublicationAntibodies(pagination);

/*
        List<PublicationStat> paginatedList = stats.stream()
                .skip(pagination.getStart())
                .limit(pagination.getLimit())
                .collect(Collectors.toList());
        response.setResults(paginatedList);
*/
        response.setHttpServletRequest(request);
        return response;
    }


}

