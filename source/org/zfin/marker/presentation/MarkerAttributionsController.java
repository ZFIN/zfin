package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.gwt.root.dto.TermNotFoundException;
import org.zfin.gwt.root.ui.DuplicateEntryException;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.service.MarkerAttributionService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.util.Comparator;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api")
public class MarkerAttributionsController {

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{markerZdbId}/attributions", method = RequestMethod.GET)
    public List<Publication> getDirectAttributionsAntibody(@PathVariable String markerZdbId) {
        return infrastructureRepository
                .getPublicationAttributions(markerZdbId)
                .stream()
                .map(PublicationAttribution::getPublication)
                .sorted(Comparator.comparing(Publication::getZdbID))
                .toList();
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{markerZdbId}/attributions", method = RequestMethod.POST)
    public Publication addDirectAttributionsAntibody(@PathVariable String markerZdbId, @RequestBody Publication publication) throws TermNotFoundException, DuplicateEntryException {
        HibernateUtil.createTransaction();
        RecordAttribution recordAttribution = MarkerAttributionService.addAttributionForMarkerID(markerZdbId, publication.getZdbID());
        HibernateUtil.flushAndCommitCurrentSession();
        return publicationRepository.getPublication(recordAttribution.getSourceZdbID());
    }

    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{markerZdbId}/attributions/{publicationID}", method = RequestMethod.GET)
    public PublicationAttribution getDirectAttributionAntibody(@PathVariable String markerZdbId, @PathVariable String publicationID) {
        Publication pub = publicationRepository.getPublication(publicationID);
        return infrastructureRepository.getPublicationAttribution(pub, markerZdbId);
    }


    @JsonView(View.MarkerRelationshipAPI.class)
    @RequestMapping(value = "/marker/{markerZdbId}/attributions/{publicationID}", method = RequestMethod.DELETE)
    public PublicationAttribution deleteDirectAttributionsAntibody(@PathVariable String markerZdbId, @PathVariable String publicationID) {
        HibernateUtil.createTransaction();

        PublicationAttribution attribution = getDirectAttributionAntibody(markerZdbId, publicationID);
        String errorMessage = MarkerAttributionService.deleteRecordAttribution(markerZdbId, publicationID);
        if (errorMessage != null) {
            HibernateUtil.rollbackTransaction();
            throw new InvalidWebRequestException("Error deleting attribution: " + errorMessage);
        }
        HibernateUtil.flushAndCommitCurrentSession();
        return attribution;
    }

}