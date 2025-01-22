package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.feature.Feature;
import org.zfin.framework.api.*;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.service.MarkerService;
import org.zfin.wiki.presentation.Version;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class MutationController {

    public MutationController() {
    }

    @Autowired
    private MarkerService markerService;

    @Autowired
    private HttpServletRequest request;

    @JsonView(View.SequenceTargetingReagentAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/strs")
    public JsonResultResponse<SequenceTargetingReagentBean> getSTRView(@PathVariable("zdbID") String zdbID,
                                                                       @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<SequenceTargetingReagentBean> response;
        try {
            response = markerService.getSTRJsonResultResponse(zdbID, pagination);
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }

        response.calculateRequestDuration(startTime);
        response.setHttpServletRequest(request);

        return response;
    }

    @JsonView(View.FeatureAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/mutations")
    public JsonResultResponse<Feature> getMutations(@PathVariable("zdbID") String zdbID,
                                                    @Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<Feature> response;
        try {
            response = markerService.getFeatureJsonResultResponse(zdbID, pagination);
        } catch (Exception e) {
            log.error("Error while retrieving ribbon details", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }

        response.calculateRequestDuration(startTime);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping("/marker/fpbase-proteins")
    protected JsonResultResponse<FluorescentProtein> showFluorescentProteins(@Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<FluorescentProtein> response;
        try {
            response = markerService.getFPBaseJsonResultResponse(pagination);
        } catch (Exception e) {
            log.error("Error while retrieving fpbase protein info", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping("/marker/efg-proteins")
    protected JsonResultResponse<FluorescentMarker> showEfgProteins(@Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<FluorescentMarker> response;
        try {
            response = markerService.getFfgFluorescenceJsonResultResponse(pagination);
        } catch (Exception e) {
            log.error("Error while retrieving fpbase protein info", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setHttpServletRequest(request);
        return response;
    }

    @JsonView(View.API.class)
    @RequestMapping("/marker/construct-proteins")
    protected JsonResultResponse<FluorescentMarker> showConstructProteins(@Version Pagination pagination) {
        long startTime = System.currentTimeMillis();
        JsonResultResponse<FluorescentMarker> response;
        try {
            response = markerService.getConstructFluorescenceJsonResultResponse(pagination);
        } catch (Exception e) {
            log.error("Error while retrieving fpbase protein info", e);
            RestErrorMessage error = new RestErrorMessage(500);
            error.addErrorMessage(e.getMessage());
            throw new RestErrorException(error);
        }
        response.calculateRequestDuration(startTime);
        response.setHttpServletRequest(request);
        return response;
    }

}