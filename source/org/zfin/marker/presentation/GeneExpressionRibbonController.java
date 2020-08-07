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
    private HttpServletRequest request;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private RibbonService ribbonService;

    @RequestMapping(value = "/marker/{geneId}/expression/ribbon-summary")
    public RibbonSummary getExpressionRibbonSummary(@PathVariable String geneId,
                                                    @RequestParam(required = false) boolean includeReporter,
                                                    @RequestParam(required = false) boolean onlyInSitu) throws Exception {
        return ribbonService.buildExpressionRibbonSummary(geneId, includeReporter, onlyInSitu);
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{geneId}/expression/ribbon-expression-detail")
    public JsonResultResponse<ExpressionDetail> getRibbonExpressionDetail(@PathVariable String geneId,
                                                                          @RequestParam(required = false) String supertermId,
                                                                          @RequestParam(required = false) String subtermId,
                                                                          @RequestParam(required = false) boolean includeReporter,
                                                                          @RequestParam(required = false) boolean onlyInSitu,
                                                                          @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<ExpressionDetail> response;
        try {
            response = ribbonService.buildExpressionDetail(geneId, supertermId, subtermId, includeReporter, onlyInSitu, pagination);
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
    @RequestMapping(value = "/marker/{geneId}/expression/ribbon-detail")
    public JsonResultResponse<ExpressionRibbonDetail> getExpressionRibbonDetail(@PathVariable String geneId,
                                                                                @RequestParam(required = false) String termId,
                                                                                @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                                @RequestParam(required = false) boolean includeReporter,
                                                                                @RequestParam(required = false) boolean onlyInSitu,
                                                                                @RequestParam(required = false) boolean isOther,
                                                                                @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        List<ExpressionRibbonDetail> allDetails;
        try {
            allDetails = ribbonService.buildExpressionRibbonDetail(geneId, termId, includeReporter, onlyInSitu, isOther);
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
            filteredList.sort(Comparator.comparing(detail -> detail.getEntity().getDisplayName().toLowerCase()));
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
    @RequestMapping(value = "/marker/{geneId}/expression/images")
    public JsonResultResponse<Image> getExpressionImages(@PathVariable String geneId,
                                                         @RequestParam(required = false) String termId,
                                                         @RequestParam(required = false) String supertermId,
                                                         @RequestParam(required = false) String subtermId,
                                                         @RequestParam(required = false) boolean includeReporter,
                                                         @RequestParam(required = false) boolean onlyInSitu,
                                                         @RequestParam(required = false) boolean isOther,
                                                         @Version Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<Image> response = expressionService.getExpressionImages(geneId, termId, supertermId, subtermId, includeReporter, onlyInSitu, isOther, pagination);
        response.setHttpServletRequest(request);
        return response;
    }

}
