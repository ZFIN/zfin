package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Image;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.api.*;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.ontology.service.RibbonService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class GeneExpressionRibbonController {

    @Autowired
    private AnatomyRepository anatomyRepository;

    @Autowired
    private OntologyRepository ontologyRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private RibbonService ribbonService;

    @RequestMapping(value = "/marker/{zdbID}/expression/ribbon-summary")
    public RibbonSummary getExpressionRibbonSummary(@PathVariable("zdbID") String zdbID,
                                                    @RequestParam(required = false) boolean includeReporter,
                                                    @RequestParam(required = false) boolean onlyDirectlySubmitted) throws Exception {
        return ribbonService.buildExpressionRibbonSummary(zdbID, includeReporter, onlyDirectlySubmitted);
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/expression/ribbon-expression-detail")
    public JsonResultResponse<ExpressionDetail> getRibbonExpressionDetail(@PathVariable("zdbID") String geneID,
                                                                          @RequestParam(value = "supertermId", required = false) String supertermID,
                                                                          @RequestParam(value = "subtermId", required = false) String subtermID,
                                                                          @RequestParam(value = "includeReporter", required = false) boolean includeReporter,
                                                                          @RequestParam(value = "onlyDirectlySubmitted", required = false) boolean onlyDirectlySubmitted,
                                                                          @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<ExpressionDetail> response;
        try {
            response = ribbonService.buildExpressionDetail(geneID, supertermID, subtermID, includeReporter, onlyDirectlySubmitted, pagination);
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setPagination(pagination);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{geneID}/expression/ribbon-detail")
    public JsonResultResponse<ExpressionRibbonDetail> getExpressionRibbonDetail(@PathVariable("geneID") String geneID,
                                                                                @RequestParam(value = "termId", required = false) String termID,
                                                                                @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                                @RequestParam(value = "includeReporter", required = false) boolean includeReporter,
                                                                                @RequestParam(value = "onlyDirectlySubmitted", required = false) boolean onlyDirectlySubmitted,
                                                                                @RequestParam(required = false) boolean isOther,
                                                                                @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        List<ExpressionRibbonDetail> allDetails;
        try {
            allDetails = ribbonService.buildExpressionRibbonDetail(geneID, termID, includeReporter, onlyDirectlySubmitted, isOther );
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(404);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }

        // filtering
        List<ExpressionRibbonDetail> filteredList = allDetails;
        if (StringUtils.isNotEmpty(filterTermName)) {
            filteredList = allDetails.stream()
                    .filter(expressionRibbonDetail -> (expressionRibbonDetail.getEntity().getDisplayName().contains(filterTermName)))
                    .collect(Collectors.toList());
        }

        // sorting
        if (filteredList != null) {
            filteredList.sort(Comparator.comparing(detail -> detail.getEntity().getDisplayName()));
        }

        // paginating
        JsonResultResponse<ExpressionRibbonDetail> response = new JsonResultResponse<>();
        if (allDetails != null) {
            response.setTotal(filteredList.size());
            response.setResults(filteredList.stream()
                    .skip(pagination.getStart())
                    .limit(pagination.getLimit())
                    .collect(Collectors.toList()));
        }
        response.addSupplementalData("stages", anatomyRepository.getAllStagesWithoutUnknown());
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);

        return response;
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/expression/images")
    public JsonResultResponse<Image> getExpressionImages(@PathVariable String zdbID,
                                                         @RequestParam(required = false) String termId,
                                                         @RequestParam(required = false) String supertermId,
                                                         @RequestParam(required = false) String subtermId,
                                                         @RequestParam(required = false) boolean includeReporter,
                                                         @RequestParam(required = false) boolean onlyDirectlySubmitted,
                                                         @RequestParam(required = false) boolean isOther,
                                                         @Version Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<Image> response = expressionService.getExpressionImages(zdbID, termId, supertermId, subtermId, includeReporter,onlyDirectlySubmitted, isOther, pagination);
        response.setHttpServletRequest(request);
        return response;
    }

}
