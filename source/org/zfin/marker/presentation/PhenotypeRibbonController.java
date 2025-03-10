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
import org.zfin.framework.api.*;
import org.zfin.mutant.PhenotypeObservationStatement;
import org.zfin.mutant.PhenotypeService;
import org.zfin.ontology.service.RibbonService;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class PhenotypeRibbonController {

    @Autowired
    private RibbonService ribbonService;

    @Autowired
    private PhenotypeService phenotypeService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AnatomyRepository anatomyRepository;

    @RequestMapping(value = "/marker/{zdbID}/phenotype/ribbon-summary")
    public RibbonSummary getPhenotypeRibbonSummary(@PathVariable("zdbID") String zdbID,
                                                   @RequestParam(required = false) boolean excludeEaps,
                                                   @RequestParam(required = false) boolean excludeSTRs) throws Exception {
        return ribbonService.buildPhenotypeRibbonSummary(zdbID, excludeEaps, excludeSTRs);
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/phenotype/summary")
    public JsonResultResponse<PhenotypeRibbonSummary> getPhenotypeSummary(@PathVariable("zdbID") String geneID,
                                                                          @RequestParam(value = "termId", required = false) String termID,
                                                                          @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                          @RequestParam(required = false) boolean isOther,
                                                                          @RequestParam(required = false) boolean excludeEaps,
                                                                          @RequestParam(required = false) boolean excludeSTRs,
                                                                          @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<PhenotypeRibbonSummary> response;
        pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterTermName);
        try {
            response = ribbonService.buildPhenotypeSummary(geneID, termID, pagination, isOther, excludeEaps, excludeSTRs);
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setPagination(pagination);
        response.setHttpServletRequest(request);
        response.addSupplementalData("stages", anatomyRepository.getAllStagesWithoutUnknown());

        return response;
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/phenotype/detail")
    public JsonResultResponse<PhenotypeObservationStatement> getPhenotypeDetail(@PathVariable("zdbID") String geneID,
                                                                                @RequestParam(value = "termId", required = false) String termID,
                                                                                @RequestParam(value = "filter.termName", required = false) String filterTermName,
                                                                                @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<PhenotypeObservationStatement> response;
        pagination.addFieldFilter(FieldFilter.FILTER_TERM_NAME, filterTermName);
        try {
            response = ribbonService.getPhenotypeDetails(geneID, termID, pagination);
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setPagination(pagination);
        response.setHttpServletRequest(request);
        response.addSupplementalData("stages", anatomyRepository.getAllStagesWithoutUnknown());

        return response;
    }

    @JsonView(View.GeneExpressionAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/phenotype/images")
    public JsonResultResponse<Image> getPhenotypeImages(@PathVariable String zdbID,
                                                        @RequestParam(required = false) String termId,
                                                        @RequestParam(required = false) boolean isOther,
                                                        @RequestParam(required = false) String phenotypeIds,
                                                        @RequestParam(required = false) boolean excludeEaps,
                                                        @RequestParam(required = false) boolean excludeSTRs,
                                                        @Version Pagination pagination) throws IOException, SolrServerException {
        JsonResultResponse<Image> response = phenotypeService.getPhenotypeImages(zdbID, termId, isOther, phenotypeIds, excludeEaps, excludeSTRs, pagination);
        response.setHttpServletRequest(request);
        return response;
    }

}
