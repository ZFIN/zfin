package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.marker.service.MarkerService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;

    @RestController
    @RequestMapping("/api")
    @Log4j2
    @Repository
    public class MarkerRelationshipPrototypeAPIController {

        public MarkerRelationshipPrototypeAPIController() {
        }

        @Autowired
        private MarkerService markerService;

        @Autowired
        private HttpServletRequest request;

        @JsonView(View.MarkerRelationshipAPI.class)
        @RequestMapping(value = "/marker/{zdbID}/relationships")
        public JsonResultResponse<MarkerRelationshipPresentation> getMarkerRelationshipView (@PathVariable("zdbID") String zdbID,
                                                                @RequestParam(value = "filter.markerRelationshiptype", required = false) String type,
                                                                @Version Pagination pagination) {
            pagination.addFieldFilter(FieldFilter.RELATIONSHIP_TYPE, type);

            JsonResultResponse<MarkerRelationshipPresentation> response = markerService.getMarkerRelationshipJsonResultResponse(zdbID, pagination);
            response.setHttpServletRequest(request);

            return response;
        }

    }


