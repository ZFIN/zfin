package org.zfin.feature.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.feature.repository.FeatureService;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Log4j2
public class FeatureAPIController {

    @Autowired private FeatureService featureService;

    @JsonView(View.API.class)
    @RequestMapping("/feature/{featureZdbID}/fish")
    public JsonResultResponse<GenotypeFishResult> getFishContainingFeature(@PathVariable String featureZdbID,
                                                                           @Version Pagination pagination,
                                                                           HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<GenotypeFishResult> response = featureService.getFishContainingFeature(featureZdbID, pagination);
        response.setHttpServletRequest(request);
        response.calculateRequestDuration(startTime);
        return response;
    }

}
