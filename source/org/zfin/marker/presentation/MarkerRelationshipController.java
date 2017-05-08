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
import org.zfin.gwt.root.dto.MarkerRelationshipDTO;
import org.zfin.gwt.root.dto.MarkerRelationshipEnumTypeGWTHack;
import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerRelationshipType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.zfin.marker.MarkerRelationship.Type.*;

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
    @RequestMapping(value = "/{markerId}/relationshipsForEdit", method = RequestMethod.GET)
    public Collection<MarkerRelationshipPresentation> getMarkerRelationshipsForEdit(@PathVariable String markerId) {
       MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();

        Marker marker = markerRepository.getMarkerByID(markerId);
        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));
        Collections.sort(cloneRelationships, markerRelationshipSupplierComparator);

        return cloneRelationships;
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
        if ((type==GENE_CONTAINS_SMALL_SEGMENT)|| (type==GENE_ENCODES_SMALL_SEGMENT) || (type==GENE_HAS_ARTIFACT )|| (type==GENE_HYBRIDIZED_BY_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG)){
                errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }

        // assume new incoming relationship has only one reference
        String pubId = newRelationship.getReferences().iterator().next().getZdbID();

        HibernateUtil.createTransaction();
        MarkerRelationship relationship = MarkerService.addMarkerRelationship(first, second, pubId, type);
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipBean.convert(relationship);
    }

    @ResponseBody
    @RequestMapping(value = "/gene-relationship", method = RequestMethod.POST)
    public Collection<MarkerRelationshipPresentation>  addGeneMarkerRelationship(@Valid @RequestBody MarkerRelationshipBean newRelationship,
                                                        BindingResult errors) {
       /* if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }*/
       String pubZDB;
        MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();
        Marker first = getMarkerByIdOrAbbrev(newRelationship.getFirst());
        Marker second = getMarkerByIdOrAbbrev(newRelationship.getSecond());
        if (second==null){
            throw new InvalidWebRequestException("Invalid marker", errors);
        }
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(newRelationship.getRelationship());

        Collection<Marker> related = MarkerService.getRelatedMarker(first, type);
        if (CollectionUtils.isNotEmpty(related) && related.contains(second)) {
          //  errors.rejectValue("second", "marker.relationship.duplicate");
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        // assume new incoming relationship has only one reference
        if (PublicationValidator.isShortVersion(newRelationship.getReferences().iterator().next().getZdbID())) {
             pubZDB=PublicationValidator.completeZdbID(newRelationship.getReferences().iterator().next().getZdbID());
        } else {
            pubZDB=newRelationship.getReferences().iterator().next().getZdbID();
        }
        Publication pub=publicationRepository.getPublication(pubZDB);
        if (pub==null){
            throw new InvalidWebRequestException("Invalid publication", errors);
        }
      //  String pubId = newRelationship.getReferences().iterator().next().getZdbID();
        if ((type==GENE_CONTAINS_SMALL_SEGMENT)|| (type==GENE_ENCODES_SMALL_SEGMENT) || (type==GENE_HAS_ARTIFACT )|| (type==GENE_HYBRIDIZED_BY_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG)){
              //  errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }
        HibernateUtil.createTransaction();
        if (!newRelationship.getRelationship().equals("clone contains gene")) {
            MarkerRelationship relationship = MarkerService.addMarkerRelationship(first, second,  pubZDB, type);
        }
        else{
            MarkerRelationship relationship = MarkerService.addMarkerRelationship(second, first,  pubZDB, type);
        }
        HibernateUtil.flushAndCommitCurrentSession();

        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(first, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(first, false));
        Collections.sort(cloneRelationships, markerRelationshipSupplierComparator);

        return cloneRelationships;

    }

    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}", method = RequestMethod.DELETE, produces = "text/plain")
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
    @RequestMapping(value = "/relationship/{relationshipId}/addreferences", method = RequestMethod.POST)
    public Collection<MarkerRelationshipPresentation> addGeneMarkerRelationshipReference(@PathVariable String relationshipId,
                                                                 @Valid @RequestBody MarkerReferenceBean newReference,
                                                                 BindingResult errors) {
        String pubZDB;
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid reference", errors);
        }
        if (PublicationValidator.isShortVersion(newReference.getZdbID())) {
            pubZDB=PublicationValidator.completeZdbID(newReference.getZdbID());
        } else {
            pubZDB=newReference.getZdbID();
        }
        MarkerRelationship relationship = markerRepository.getMarkerRelationshipByID(relationshipId);
        Publication publication = publicationRepository.getPublication(pubZDB);

        for (PublicationAttribution reference : relationship.getPublications()) {
            if (reference.getPublication().equals(publication)) {
                errors.rejectValue("zdbID", "marker.reference.inuse");
                throw new InvalidWebRequestException("Invalid reference", errors);
            }
        }

        HibernateUtil.createTransaction();
        markerRepository.addMarkerRelationshipAttribution(relationship, publication, relationship.getFirstMarker());
        HibernateUtil.flushAndCommitCurrentSession();
        MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();
        Marker first = relationship.getFirstMarker();
        Marker second = relationship.getSecondMarker();
        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(first, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(first, false));
        Collections.sort(cloneRelationships, markerRelationshipSupplierComparator);

        return cloneRelationships;
    }

    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}/references/{pubID}", method = RequestMethod.DELETE, produces = "text/plain")
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
