package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;

import java.util.ArrayList;
import java.util.Collection;

@Controller
@RequestMapping("/marker")
public class MarkerRelationshipController {

    @Autowired
    private MarkerService markerService;

    @Autowired
    private MarkerRepository markerRepository;

    @ResponseBody
    @RequestMapping(value = "/{markerId}/relationships", method = RequestMethod.GET)
    public Collection<MarkerRelationshipBean> getMarkerRelationships(@PathVariable String markerId) {
        Marker marker = markerRepository.getMarkerByID(markerId);
        Collection<MarkerRelationship> allRelationships = new ArrayList<>();
        allRelationships.addAll(marker.getFirstMarkerRelationships());
        allRelationships.addAll(marker.getSecondMarkerRelationships());
        Collection<MarkerRelationshipBean> beans = new ArrayList<>();
        for (MarkerRelationship relationship : allRelationships) {
            MarkerRelationshipBean bean = new MarkerRelationshipBean();
            bean.setRelationship(relationship.getType().toString());
            bean.setFirst(DTOConversionService.convertToMarkerDTO(relationship.getFirstMarker()));
            bean.setSecond(DTOConversionService.convertToMarkerDTO(relationship.getSecondMarker()));
            Collection<MarkerReferenceBean> references = new ArrayList<>();
            for (PublicationAttribution reference : relationship.getPublications()) {
                MarkerReferenceBean referenceBean = new MarkerReferenceBean();
                referenceBean.setZdbID(reference.getSourceZdbID());
                referenceBean.setTitle(reference.getPublication().getTitle());
                references.add(referenceBean);
            }
            bean.setReferences(references);
            beans.add(bean);
        }
        return beans;
    }

}
