package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.infrastructure.InfrastructureService;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptStatus;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.profile.service.BeanCompareService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class TranscriptDetailsController {

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private BeanCompareService beanCompareService;

    @Autowired
    private InfrastructureRepository infrastructureRepository;
    private transient Logger logger = LogManager.getLogger(TranscriptDetailsController.class);
    @Autowired
    private HttpServletRequest request;

    @InitBinder("transcriptAttributeBean")
    public void initRelationshipBinder(WebDataBinder binder) {
        binder.setValidator(new TranscriptUpdateValidator());
    }


    @JsonView(View.TranscriptDetailsAPI.class)
    @ResponseBody
    @RequestMapping(value = "/transcript/{TranscriptZdbId}/details", method = RequestMethod.GET)
    public TranscriptAttributeBean getTranscriptDetails(@PathVariable String TranscriptZdbId) {
        Transcript transcript = markerRepository.getTranscriptByZdbID(TranscriptZdbId);
        TranscriptAttributeBean transcriptDTO = new TranscriptAttributeBean();

        transcriptDTO.setTranscriptType(transcript.getTranscriptType().getType().toString());
        if (transcript.getStatus() != null) {
            transcriptDTO.setTranscriptStatus(transcript.getStatus().getStatus().toString());
        } else {
            transcriptDTO.setTranscriptStatus("");
        }

        transcriptDTO.setReferences(transcript.getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .collect(Collectors.toList()));

        return transcriptDTO;

    }

    @SneakyThrows
    @JsonView(View.TranscriptDetailsAPI.class)
    @RequestMapping(value = "/transcript/{TranscriptZdbId}/details", method = RequestMethod.POST)
    public TranscriptAttributeBean updateTranscriptDetails(@PathVariable String TranscriptZdbId,
                                                           @RequestBody TranscriptAttributeBean formData, BindingResult result) {
        Transcript transcript = markerRepository.getTranscriptByZdbID(TranscriptZdbId);

        // create transcript
        if (result.hasErrors()) {

            throw new InvalidWebRequestException("Invalid transcript status-type", result);
        }
        //


        Session session = HibernateUtil.currentSession();
        Transaction transaction = session.beginTransaction();

        TranscriptType newType = RepositoryFactory.getMarkerRepository().getTranscriptTypeForName(formData.getTranscriptType());
        if (!transcript.getTranscriptType().equals(newType)) {
            TranscriptType oldType = transcript.getTranscriptType();
            transcript.setTranscriptType(newType);
            InfrastructureService.insertUpdate(transcript, "Transcript Type", oldType.getDisplay(), newType.getDisplay());
        }


        TranscriptStatus oldStatus = transcript.getStatus();
        TranscriptStatus newStatus;
        if (formData.getTranscriptStatus() == null
                || formData.getTranscriptStatus().equals("null")
                || formData.getTranscriptStatus().equals("")) {
            newStatus = null;
        } else {
            newStatus = RepositoryFactory.getMarkerRepository().getTranscriptStatusForName(formData.getTranscriptStatus());
        }


        transcript.setStatus(newStatus);

        String oldStatusString = (oldStatus == null) ? "null" : oldStatus.getDisplay();
        String newStatusString = (newStatus == null) ? "null" : newStatus.getDisplay();

        InfrastructureService.insertUpdate(transcript, "Transcript Status", oldStatusString, newStatusString);

        Collection<String> currentPubIds = transcript.getPublications().stream()
                .map(PublicationAttribution::getPublication)
                .map(Publication::getZdbID)
                .collect(Collectors.toList());
        Collection<String> updatedPubIds = formData.getReferences().stream()
                .map(Publication::getZdbID)
                .collect(Collectors.toList());
        Collection<String> pubsToAdd = CollectionUtils.subtract(updatedPubIds, currentPubIds);
        Collection<String> pubsToRemove = CollectionUtils.subtract(currentPubIds, updatedPubIds);


        for (String pubId : pubsToAdd) {
            Publication publication = publicationRepository.getPublication(pubId);
            markerRepository.addMarkerPub(transcript, publication);
        }

        for (String pubToRemove : pubsToRemove) {
            infrastructureRepository.deleteRecordAttribution(transcript.getZdbID(), pubToRemove);
        }

        HibernateUtil.currentSession().save(transcript);

        HibernateUtil.flushAndCommitCurrentSession();
        return TranscriptAttributeBean.convert(transcript);
    }


}


