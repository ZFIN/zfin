package org.zfin.marker.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
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
    private MarkerRepository markerRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

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
    public MarkerRelationshipBean addMarkerRelationship(@RequestBody MarkerRelationshipBean newRelationship) {
        Marker first = markerRepository.getMarkerByID(newRelationship.getFirst().getZdbID());
        Marker second = markerRepository.getMarkerByID(newRelationship.getSecond().getZdbID());
        MarkerRelationship.Type type = MarkerRelationship.Type.getType(newRelationship.getRelationship());
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
                                                                 @RequestBody MarkerReferenceBean newReference) {

        HibernateUtil.createTransaction();
        infrastructureRepository.insertRecordAttribution(relationshipId, newReference.getZdbID());
        HibernateUtil.flushAndCommitCurrentSession();

        return MarkerRelationshipBean.convert(
                markerRepository.getMarkerRelationshipByID(relationshipId));
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

}
