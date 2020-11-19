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
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class MarkerRelationshipAPIController {

    @Autowired private MarkerService markerService;
    @Autowired private MarkerRepository markerRepository;
    @Autowired private HttpServletRequest request;

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/relationships")
    public JsonResultResponse<MarkerRelationshipPresentation> getMarkerRelationshipView(
            @PathVariable String zdbID,
            @RequestParam(value = "filter.markerRelationshipType", required = false) String type,
            @Version Pagination pagination) {
        pagination.addFieldFilter(FieldFilter.RELATIONSHIP_TYPE, type);

        JsonResultResponse<MarkerRelationshipPresentation> response = markerService.getMarkerRelationshipJsonResultResponse(zdbID, pagination);
        response.setHttpServletRequest(request);

        return response;
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/editableRelationships")
    public List<MarkerRelationship> getMarkerRelationshipEdit(@PathVariable String zdbID,
                                                              @RequestParam(required = false) String relationshipTypes) {
        Marker marker = markerRepository.getMarker(zdbID);

        List<MarkerRelationship> allRelationships = new ArrayList<>();
        allRelationships.addAll(marker.getFirstMarkerRelationships());
        allRelationships.addAll(marker.getSecondMarkerRelationships());
        if (relationshipTypes != null) {
            List<String> types = Arrays.asList(relationshipTypes.split("\\s*,\\s*"));
            allRelationships.removeIf(markerRelationship -> !types.contains(markerRelationship.getType().toString()));
        }
        allRelationships.sort(Comparator
                .comparing(MarkerRelationship::getMarkerRelationshipType)
                .thenComparing(rel -> rel.getFirstMarker().getAbbreviationOrder())
                .thenComparing(rel -> rel.getSecondMarker().getAbbreviationOrder())
        );
        return allRelationships;
    }

}


