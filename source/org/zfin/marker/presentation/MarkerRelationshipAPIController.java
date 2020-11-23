package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.JsonResultResponse;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.wiki.presentation.Version;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Log4j2
@Repository
public class MarkerRelationshipAPIController {

    @Autowired
    private MarkerService markerService;
    @Autowired
    private MarkerRepository markerRepository;
    @Autowired
    private HttpServletRequest request;

    @InitBinder("markerRelationshipFormBean")
    public void initRelationshipBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerRelationshipFormBeanValidator());
    }

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
    public List<MarkerRelationshipFormBean> getMarkerRelationshipEdit(@PathVariable String zdbID,
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
        return allRelationships.stream()
                .map(MarkerRelationshipFormBean::convert)
                .collect(Collectors.toList());
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{zdbID}/relationships", method = RequestMethod.POST)
    public MarkerRelationshipFormBean addMarkerRelationship(@PathVariable String zdbID,
                                                            @Valid @RequestBody MarkerRelationshipFormBean form,
                                                            BindingResult errors) {
        // this validates that the both markers exist, that at least one pub is specified, and that all pub IDs
        // are valid. because STR relationship types get fudged by a controller method this has to be validated
        // in a second step. Once the STR edit interface is updated, that validation should be able to be done
        // in MarkerRelationshipFormBeanValidator.validate() as well.
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        Marker firstMarker = markerService.getMarkerByIdOrAbbreviation(form.getFirstMarker().getZdbID(), form.getFirstMarker().getAbbreviation());
        Marker secondMarker = markerService.getMarkerByIdOrAbbreviation(form.getSecondMarker().getZdbID(), form.getSecondMarker().getAbbreviation());
        MarkerRelationshipType markerRelationshipType = markerRepository.getMarkerRelationshipType(form.getMarkerRelationshipType().getName());
        MarkerRelationshipFormBeanValidator.validateMarkerRelationshipType(firstMarker, secondMarker, markerRelationshipType, errors);
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        Collection<Marker> related = MarkerService.getRelatedMarkers(firstMarker, markerRelationshipType);
        if (CollectionUtils.isNotEmpty(related) && related.contains(secondMarker)) {
            errors.reject("marker.relationship.duplicate");
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        Iterator<Publication> publicationIterator = form.getReferences().iterator();

        HibernateUtil.createTransaction();
        MarkerRelationship relationship = MarkerService.addMarkerRelationship(firstMarker, secondMarker, publicationIterator.next(), markerRelationshipType);
        while (publicationIterator.hasNext()) {
            markerRepository.addMarkerRelationshipAttribution(relationship, publicationIterator.next());
        }
        HibernateUtil.flushAndCommitCurrentSession();

        // in order for the next fetch to pick up all of the record attributions
        HibernateUtil.currentSession().evict(relationship);

        return MarkerRelationshipFormBean.convert(markerRepository.getMarkerRelationshipByID(relationship.getZdbID()));
    }

}


