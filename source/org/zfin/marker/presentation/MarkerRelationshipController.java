package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;

import jakarta.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/marker")
public class MarkerRelationshipController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;
    private static Logger logger = LogManager.getLogger(MarkerRelationshipController.class);
    @InitBinder("markerRelationshipFormBean")
    public void initRelationshipBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerRelationshipFormBeanValidator());
    }

    @InitBinder("markerReferenceBean")
    public void initReferenceBinder(WebDataBinder binder) {
        binder.setValidator(new MarkerReferenceBeanValidator());
    }



    @ResponseBody
    @RequestMapping("/{markerId}/relationshipTypes")
    public Collection<String> getRelationshipTypes(@PathVariable String markerId,@RequestParam(name = "interacts", required = true) Boolean interacts) {

        Marker marker = markerRepository.getMarkerByID(markerId);


        List<String> relType = markerRepository.getMarkerRelationshipTypesForMarkerEdit(marker,interacts);

        return relType;
    }



    @JsonView(View.MarkerRelationshipAPI.class)
    @ResponseBody
    @RequestMapping(value = "/{markerId}/relationships", method = RequestMethod.GET)
    public Collection<MarkerRelationshipFormBean> getMarkerRelationships(@PathVariable String markerId) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        Collection<MarkerRelationship> allRelationships = new ArrayList<>();
        allRelationships.addAll(marker.getFirstMarkerRelationships());
        allRelationships.addAll(marker.getSecondMarkerRelationships());
        Collection<MarkerRelationshipFormBean> beans = new ArrayList<>();
        for (MarkerRelationship relationship : allRelationships) {
            beans.add(MarkerRelationshipFormBean.convert(relationship));
        }
        return beans;
        }

    @ResponseBody
    @RequestMapping(value = "/{markerId}/relationshipsForEdit", method = RequestMethod.GET)
    public Collection<MarkerRelationshipPresentation> getMarkerRelationshipsForEdit(@PathVariable String markerId,@RequestParam(name = "interacts", required = true) Boolean interacts) {
       MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();

        Marker marker = markerRepository.getMarkerByID(markerId);
        List<MarkerRelationshipPresentation> cloneRelationships = new ArrayList<>();

        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, true));
        cloneRelationships.addAll(MarkerService.getRelatedMarkerDisplayExcludeType(marker, false));
        cloneRelationships.addAll(markerRepository.getWeakReferenceMarker(marker.getZdbID()
                        , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                        , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT));

        Set<MarkerRelationshipPresentation> nonDuplicateCloneRelashionshipsSet = new HashSet<>();
        for (MarkerRelationshipPresentation cloneRelationship : cloneRelationships) {
            nonDuplicateCloneRelashionshipsSet.add(cloneRelationship);
        }

        List<MarkerRelationshipPresentation> nonDuplicateCloneRelashionships = new ArrayList<>();
        for (MarkerRelationshipPresentation nonDuplicateCloneRelationship : nonDuplicateCloneRelashionshipsSet) {
            nonDuplicateCloneRelashionships.add(nonDuplicateCloneRelationship);
        }

        Collections.sort(nonDuplicateCloneRelashionships, markerRelationshipSupplierComparator);
        if (!interacts) {
            for (int i = 0; i < nonDuplicateCloneRelashionships.size(); i++) {
                MarkerRelationshipPresentation mrp = nonDuplicateCloneRelashionships.get(i);



                if (mrp.getRelationshipType().contains("interacts with")) {
                    nonDuplicateCloneRelashionships.remove(i);
                }
            }
        }

            if (interacts) {
                for (int i = 0; i < nonDuplicateCloneRelashionships.size(); i++) {
                    MarkerRelationshipPresentation mrp = nonDuplicateCloneRelashionships.get(i);

                if (!mrp.getRelationshipType().equals("interacts with")) {

                    nonDuplicateCloneRelashionships.remove(i);
                    i--;
                }
            }

    }

  /*if (interacts.equals("no")) {
            cloneRelationships.removeIf(s -> (s.getName().contains("interact")));
        }
        if (interacts.equals("yes")) {
            cloneRelationships.removeIf(s -> !(s.getName().contains("interact")));
        }*/


        return nonDuplicateCloneRelashionships;
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @ResponseBody
    @RequestMapping(value = "/relationship", method = RequestMethod.POST)
    public MarkerRelationshipFormBean addMarkerRelationship(@Valid @RequestBody MarkerRelationshipFormBean newRelationship,
                                                            BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }
        MarkerRelationship.Type mkrType;
        Marker first = getMarkerByIdOrAbbrev(newRelationship.getFirstMarker());
        Marker second = getMarkerByIdOrAbbrev(newRelationship.getSecondMarker());
         mkrType = MarkerRelationship.Type.getType(newRelationship.getMarkerRelationshipType().getName());
        if (second.isInTypeGroup(Marker.TypeGroup.NONTSCRBD_REGION)){
            if (first.getType()==Marker.Type.CRISPR){
                 mkrType = MarkerRelationship.Type.CRISPR_TARGETS_REGION;
            }
            if (first.getType()==Marker.Type.TALEN){
                mkrType = MarkerRelationship.Type.TALEN_TARGETS_REGION;
            }
            if (first.getType()==Marker.Type.MRPHLNO){
                errors.rejectValue("secondMarker", "marker.relationship.duplicate");
                throw new InvalidWebRequestException("Invalid marker relationship", errors);
            }
        }


        Collection<Marker> related = MarkerService.getRelatedMarker(first, mkrType);
        if (CollectionUtils.isNotEmpty(related) && related.contains(second)) {
            errors.rejectValue("secondMarker", "marker.relationship.duplicate");
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }
        /*if ((type==GENE_CONTAINS_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG_NO_ESTCDNA)){
                errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }
        if ((type==GENE_ENCODES_SMALL_SEGMENT) || (type==GENE_HAS_ARTIFACT )|| (type==GENE_HYBRIDIZED_BY_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG)){
                errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }*/

        // assume new incoming relationship has only one reference
        String pubId = newRelationship.getReferences().iterator().next().getZdbID();

        HibernateUtil.createTransaction();

        MarkerRelationship relationship = MarkerService.addMarkerRelationship(first, second, pubId, mkrType);
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipFormBean.convert(relationship);
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @ResponseBody
    @RequestMapping(value = "/gene-relationship", method = RequestMethod.POST)
    public Collection<MarkerRelationshipPresentation>  addGeneMarkerRelationship(@Valid @RequestBody MarkerRelationshipFormBean newRelationship,
                                                        BindingResult errors) {
       /* if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }*/
       String pubZDB;
        MarkerRelationshipSupplierComparator markerRelationshipSupplierComparator = new MarkerRelationshipSupplierComparator();
        Marker first = getMarkerByIdOrAbbrev(newRelationship.getFirstMarker());
        Marker second = getMarkerByIdOrAbbrev(newRelationship.getSecondMarker());
        if (second==null){
            throw new InvalidWebRequestException("Invalid marker", errors);
        }
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(newRelationship.getMarkerRelationshipType().getName());

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
        List<String> markerTypes=markerRepository.getMarkerTypesforRelationship(type.toString());
        String markerTypeDisplay1=String.join(",", markerTypes);
        String markerTypeDisplay=markerTypeDisplay1.replaceAll(",", "\n");
        if (!markerTypes.contains(second.getMarkerType().getName())){
            throw new InvalidWebRequestException("You can only add markers of type " + markerTypeDisplay.replaceAll(",", "\n") + " to this marker relationship", errors);
        }

        /*if ((type==GENE_CONTAINS_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG_NO_ESTCDNA)){
                errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }
        if ((type==GENE_ENCODES_SMALL_SEGMENT) || (type==GENE_HAS_ARTIFACT )|| (type==GENE_HYBRIDIZED_BY_SMALL_SEGMENT)){
            if (!second.isInTypeGroup(Marker.TypeGroup.SMALLSEG)){
                errors.rejectValue("second", "marker.relationship.invalid");
                throw new InvalidWebRequestException("You cannnot enter this relationship type to this marker type", errors);
            }
        }*/

        HibernateUtil.createTransaction();
        if (!newRelationship.getMarkerRelationshipType().getName().equals("clone contains gene")) {
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

    @JsonView(View.MarkerRelationshipAPI.class)
    @ResponseBody
    @RequestMapping(value = "/relationship/{relationshipId}/references", method = RequestMethod.POST)
    public MarkerRelationshipFormBean addMarkerRelationshipReference(@PathVariable String relationshipId,
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
        markerRepository.addMarkerRelationshipAttribution(relationship, publication);
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipFormBean.convert(relationship);
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

    private Marker getMarkerByIdOrAbbrev(Marker marker) {
        if (StringUtils.isNotEmpty(marker.getZdbID())) {
            return markerRepository.getMarkerByID(marker.getZdbID());
        } else if (StringUtils.isNotEmpty(marker.getAbbreviation())) {
            return markerRepository.getMarkerByAbbreviation(marker.getAbbreviation());
        }
        return null;
    }

}
