package org.zfin.feature.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.repository.RepositoryFactory;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Log4j2
public class FeatureAPIController {

    @Autowired
    private FeatureService featureService;

    @JsonView(View.API.class)
    @RequestMapping("/feature/{featureZdbID}/fish")
    public JsonResultResponse<GenotypeFishResult> getFishContainingFeature(@PathVariable String featureZdbID,
                                                                           @RequestParam(required = false) boolean excludeFishWithSTR,
                                                                           @Version Pagination pagination,
                                                                           HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        HibernateUtil.createTransaction();
        JsonResultResponse<GenotypeFishResult> response = featureService.getFishContainingFeature(featureZdbID, excludeFishWithSTR, pagination);
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);
        return response;
    }

    @JsonView(View.FeatureAPI.class)
    @RequestMapping(value = "/lab/{zdbID}/features")
    public JsonResultResponse<Feature> getFeatureForLab(@PathVariable String zdbID,
                                                        @Version Pagination pagination,
                                                        HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        PaginationResult<Feature> paginationResult = RepositoryFactory.getFeatureRepository().getFeaturesForLab(zdbID, pagination);
        JsonResultResponse<Feature> response = new JsonResultResponse<>();
        response.setResults(paginationResult.getPopulatedResults());
        response.setTotal(paginationResult.getTotalCount());
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);
        return response;
    }


}
