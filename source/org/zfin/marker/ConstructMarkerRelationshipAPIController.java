package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.ConstructRelationship;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.presentation.MarkerRelationshipFormBean;
import org.zfin.marker.presentation.MarkerRelationshipFormBeanValidator;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.ConstructService;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;

import jakarta.validation.Valid;
import java.util.Collection;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;

@RestController
@RequestMapping("/api")
@Log4j2
public class ConstructMarkerRelationshipAPIController {

    @Autowired
    private MarkerRepository markerRepository;

    @JsonView(View.API.class)
    @RequestMapping(value = "/construct/{zdbID}/relationships", method = RequestMethod.POST)
    public ConstructRelationship addConstructMarkerRelationship(@PathVariable String zdbID,
                                                            @Valid @RequestBody MarkerRelationshipFormBean form,
                                                            BindingResult errors) {
        if (errors.hasErrors()) {
            throw new InvalidWebRequestException("Invalid marker relationship", errors);
        }

        Marker firstMarker = markerRepository.getMarkerByID(zdbID);
        Marker secondMarker = markerRepository.getMarkerByID(form.getSecondMarker().getZdbID());

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

        Publication publication = form.getReferences().iterator().next();

        HibernateUtil.createTransaction();

        String constructZdbID = ConstructService.addConstructMarkerRelationship(firstMarker.getZdbID(), secondMarker.getZdbID(), markerRelationshipType.getName(), publication.getZdbID());

        HibernateUtil.flushAndCommitCurrentSession();

        return getConstructRepository().getConstructRelationshipByID(constructZdbID);
    }

    @ResponseBody
    @RequestMapping(value = "/construct/{zdbID}/relationships/{cmrelZdbID}", method = RequestMethod.DELETE)
    public String deleteConstructMarkerRelationship(@PathVariable String zdbID, @PathVariable String cmrelZdbID) {
        ConstructRelationship cmrel = getConstructRepository().getConstructRelationshipByID(cmrelZdbID);
        ConstructCuration construct = cmrel.getConstruct();
        if (!construct.getZdbID().equals(zdbID)) {
            throw new RuntimeException("Deleting construct relationship for wrong construct");
        }
        HibernateUtil.createTransaction();
        ConstructService.deleteConstructRelationship(cmrelZdbID);
        HibernateUtil.flushAndCommitCurrentSession();
        return cmrelZdbID;
    }

}


