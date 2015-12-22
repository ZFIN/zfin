package org.zfin.marker.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/marker")
public class MarkerRelationshipController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @InitBinder("markerRelationshipBean")
    public void initRelationshipBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerRelationshipBeanValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/relationships", method = RequestMethod.GET)
    public Collection<MarkerRelationshipBean> getMarkerRelationships(@PathVariable String markerId) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        Collection<MarkerRelationship> allRelationships = new ArrayList<>();
        allRelationships.addAll(marker.getFirstMarkerRelationships());
        allRelationships.addAll(marker.getSecondMarkerRelationships());
        Collection<MarkerRelationshipBean> beans = new ArrayList<>();
        for (MarkerRelationship relationship : allRelationships) {
            beans.add(MarkerRelationshipBean.convert(relationship));
        }
        return beans;
    }

    @ResponseBody
    @RequestMapping(value = "/relationship", method = RequestMethod.POST)
    public MarkerRelationshipBean addMarkerRelationship(@Valid @RequestBody MarkerRelationshipBean newRelationship,
                                                        BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        Marker first = getMarkerByIdOrAbbrev(newRelationship.getFirst());
        Marker second = getMarkerByIdOrAbbrev(newRelationship.getSecond());
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(newRelationship.getRelationship());

        Collection<Marker> related = MarkerService.getRelatedMarker(first, type);
        if (CollectionUtils.isNotEmpty(related) && related.contains(second)) {
            errors.rejectValue("second", "marker.relationship.duplicate");
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        // assume new incoming relationship has only one reference
        String pubId = newRelationship.getReferences().iterator().next().getZdbID();

        HibernateUtil.createTransaction();
        MarkerRelationship relationship = MarkerService.addMarkerRelationship(first, second, pubId, type);
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipBean.convert(relationship);
    }

    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}", method = RequestMethod.DELETE)
    public String removeMarkerRelationship(@PathVariable String relationshipId) {
        MarkerRelationship relationship = markerRepository.getMarkerRelationshipByID(relationshipId);

        HibernateUtil.createTransaction();
        markerRepository.deleteMarkerRelationship(relationship);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}/references", method = RequestMethod.POST)
    public MarkerRelationshipBean addMarkerRelationshipReference(@PathVariable String relationshipId,
                                                                 @Valid @RequestBody MarkerReferenceBean newReference,
                                                                 BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid reference", errors);
        }

        MarkerRelationship relationship = markerRepository.getMarkerRelationshipByID(relationshipId);
        Publication publication = publicationRepository.getPublication(newReference.getZdbID());

        for (PublicationAttribution reference : relationship.getPublications()) {
            if (reference.getPublication().equals(publication)) {
                errors.rejectValue("zdbID", "marker.reference.inuse");
                throw new InvalidWebRequestException("Invalid reference", errors);
            }
        }

        HibernateUtil.createTransaction();
        markerRepository.addMarkerRelationshipAttribution(relationship, publication, relationship.getFirstMarker());
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipBean.convert(relationship);
    }

    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}/references/{pubID}", method = RequestMethod.DELETE)
    public String removeMarkerRelationshipReference(@PathVariable String relationshipId,
                                                    @PathVariable String pubID) {
        HibernateUtil.createTransaction();
        infrastructureRepository.deleteRecordAttribution(relationshipId, pubID);
        HibernateUtil.flushAndCommitCurrentSession();

        return "OK";
    }

    private Marker getMarkerByIdOrAbbrev(MarkerDTO dto) {
        if (dto.getZdbID() != null) {
            return markerRepository.getMarkerByID(dto.getZdbID());
        } else if (dto.getName() != null) {
            return markerRepository.getMarkerByAbbreviation(dto.getName());
        }
        return null;
    }

}
