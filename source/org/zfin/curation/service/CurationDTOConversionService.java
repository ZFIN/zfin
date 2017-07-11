package org.zfin.curation.service;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.*;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.*;
import org.zfin.publication.presentation.DashboardImageBean;
import org.zfin.publication.presentation.DashboardPublicationBean;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurationDTOConversionService {

    @Autowired
    private ProfileRepository profileRepository;

    private static final String DEFAULT_IMAGE = "/images/LOCAL/smallogo.gif";

    public PublicationNoteDTO toPublicationNoteDTO(PublicationNote note) {
        PublicationNoteDTO dto = new PublicationNoteDTO();
        dto.setZdbID(note.getZdbID());
        dto.setDate(note.getDate());
        dto.setText(note.getText());
        dto.setCurator(toPersonDTO(note.getCurator()));

        Person currentUser = ProfileService.getCurrentSecurityUser();
        dto.setEditable(
                currentUser != null &&
                !currentUser.getShortName().equals("Guest") &&
                currentUser.equals(note.getCurator()));

        return dto;
    }

    public PersonDTO toPersonDTO(Person person) {
        if (person == null) {
            return null;
        }
        PersonDTO dto = new PersonDTO();
        dto.setZdbID(person.getZdbID());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setName(person.getFirstName() + " " + person.getLastName());
        dto.setEmail(person.getEmail());
        if (StringUtils.isNotEmpty(person.getImage())) {
            dto.setImageURL(ZfinPropertiesEnum.IMAGE_LOAD.value() + "/" + person.getImage());
        } else {
            dto.setImageURL(DEFAULT_IMAGE);
        }
        return dto;
    }

    public PersonDTO toPersonDTO(CorrespondenceRecipient recipient) {
        if (recipient.getPerson() != null) {
            return toPersonDTO(recipient.getPerson());
        }
        PersonDTO dto = new PersonDTO();
        dto.setEmail(recipient.getEmail());
        return dto;
    }

    public CurationDTO toCurationDTO(Curation curation) {
        CurationDTO dto = new CurationDTO();
        dto.setZdbID(curation.getZdbID());
        dto.setTopic(curation.getTopic().toString());
        dto.setCurator(toPersonDTO(curation.getCurator()));
        dto.setDataFound(curation.isDataFound());
        dto.setEntryDate(curation.getEntryDate());
        dto.setOpenedDate(curation.getOpenedDate());
        dto.setClosedDate(curation.getClosedDate());
        return dto;
    }

    public CorrespondenceDTO toCorrespondenceDTO(CorrespondenceSentMessage sent) {
        CorrespondenceDTO dto = new CorrespondenceDTO();
        dto.setId(sent.getId());
        dto.setPub(sent.getPublication().getZdbID());
        dto.setOutgoing(true);
        dto.setDate(sent.getSentDate());
        dto.setComposedDate(sent.getMessage().getComposedDate());
        dto.setFrom(toPersonDTO(sent.getFrom()));
        dto.setTo(sent.getMessage().getRecipients().stream()
                .map(this::toPersonDTO)
                .collect(Collectors.toList()));
        dto.setSubject(sent.getMessage().getSubject());
        dto.setMessage(sent.getMessage().getText());
        dto.setResend(sent.isResend());
        return dto;
    }

    public CorrespondenceDTO toCorrespondenceDTO(CorrespondenceReceivedMessage received) {
        CorrespondenceDTO dto = new CorrespondenceDTO();
        dto.setId(received.getId());
        dto.setPub(received.getPublication().getZdbID());
        dto.setOutgoing(false);
        dto.setDate(received.getDate());
        PersonDTO from = new PersonDTO();
        from.setEmail(received.getFromEmail());
        dto.setFrom(from);
        dto.setTo(Collections.singletonList(toPersonDTO(received.getTo())));
        dto.setSubject(received.getSubject());
        dto.setMessage(received.getText());
        dto.setResend(false);
        return dto;
    }

    public Collection<CurationDTO> allCurationTopics(Collection<Curation> curationRecords) {
        Set<CurationDTO> curationSet = new TreeSet<>(new Comparator<CurationDTO>() {
            @Override
            public int compare(CurationDTO o1, CurationDTO o2) {
                return o1.getTopic().compareTo(o2.getTopic());
            }
        });
        List<Curation.Topic> existingTopics = new ArrayList<>();
        // Add curation records which exist
        for (Curation c : curationRecords) {
            curationSet.add(toCurationDTO(c));
            existingTopics.add(c.getTopic());
        }
        // Add placeholder records for other topics
        for (Curation.Topic t : Curation.Topic.values()) {
            if (!existingTopics.contains(t) && t != Curation.Topic.LINKED_AUTHORS) {
                CurationDTO dto = new CurationDTO();
                dto.setTopic(t.toString());
                curationSet.add(dto);
            }
        }
        return curationSet;
    }

    public CurationStatusDTO toCurationStatusDTO(PublicationTrackingHistory status) {
        if (status == null) {
            return null;
        }
        CurationStatusDTO dto = new CurationStatusDTO();
        dto.setCurrent(status.isCurrent());
        dto.setPubZdbID(status.getPublication().getZdbID());
        dto.setStatus(status.getStatus());
        dto.setLocation(status.getLocation());
        dto.setOwner(toPersonDTO(status.getOwner()));
        dto.setUpdateDate(status.getDate());
        return dto;
    }

    public DashboardPublicationBean toDashboardPublicationBean(PublicationTrackingHistory status) {
        if (status == null) {
            return null;
        }
        Publication publication = status.getPublication();

        DashboardPublicationBean bean = new DashboardPublicationBean();
        bean.setZdbId(publication.getZdbID());
        bean.setTitle(publication.getTitle());
        bean.setCitation(publication.getJournalAndPages());
        bean.setAuthors(publication.getAuthors());
        bean.setAbstractText(publication.getAbstractText());
        bean.setStatus(toCurationStatusDTO(status));
        bean.setPdfPath(publication.getFileName());
        bean.setLastCorrespondenceDate(publication.getLastSentEmailDate());
        List<DashboardImageBean> images = new ArrayList<>();
        for (Figure figure : publication.getFigures()) {
            for (Image image : figure.getImages()) {
                DashboardImageBean imageBean = new DashboardImageBean();
                imageBean.setLabel(figure.getLabel());
                imageBean.setFullPath(image.getUrl());
                imageBean.setMediumPath(image.getMediumUrl());
                images.add(imageBean);
            }
        }
        images.sort((o1, o2) -> ObjectUtils.compare(o1.getLabel(), o2.getLabel()));
        bean.setImages(images);
        return bean;
    }

}
