package org.zfin.curation.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.zfin.curation.Correspondence;
import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.*;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.presentation.DashboardImageBean;
import org.zfin.publication.presentation.DashboardPublicationBean;

import java.util.*;

@Service
public class CurationDTOConversionService {

    private static final String DEFAULT_IMAGE = "/images/LOCAL/smallogo.gif";

    public PublicationNoteDTO toPublicationNoteDTO(PublicationNote note) {
        PublicationNoteDTO dto = new PublicationNoteDTO();
        dto.setZdbID(note.getZdbID());
        dto.setDate(note.getDate());
        dto.setText(note.getText());
        dto.setCurator(toCuratorDTO(note.getCurator()));

        Person currentUser = ProfileService.getCurrentSecurityUser();
        dto.setEditable(
                currentUser != null &&
                !currentUser.getShortName().equals("Guest") &&
                currentUser.equals(note.getCurator()));

        return dto;
    }

    public CuratorDTO toCuratorDTO(Person curator) {
        if (curator == null) {
            return null;
        }
        CuratorDTO dto = new CuratorDTO();
        dto.setZdbID(curator.getZdbID());
        dto.setName(curator.getFirstName() + " " + curator.getLastName());
        if (curator.getSnapshot() == null) {
            dto.setImageURL(DEFAULT_IMAGE);
        } else {
            dto.setImageURL("/action/profile/image/view/" + curator.getZdbID() + ".jpg");
        }
        return dto;
    }

    public CurationDTO toCurationDTO(Curation curation) {
        CurationDTO dto = new CurationDTO();
        dto.setZdbID(curation.getZdbID());
        dto.setTopic(curation.getTopic().toString());
        dto.setCurator(toCuratorDTO(curation.getCurator()));
        dto.setDataFound(curation.isDataFound());
        dto.setEntryDate(curation.getEntryDate());
        dto.setOpenedDate(curation.getOpenedDate());
        dto.setClosedDate(curation.getClosedDate());
        return dto;
    }

    public CorrespondenceDTO toCorrespondenceDTO(Correspondence correspondence) {
        CorrespondenceDTO dto = new CorrespondenceDTO();
        dto.setPub(correspondence.getPublication().getZdbID());
        dto.setCurator(toCuratorDTO(ProfileService.getCurrentSecurityUser()));
        dto.setId(correspondence.getId());
        dto.setOpenedDate(correspondence.getContactedDate());
        dto.setReplyReceived(correspondence.getRespondedDate() != null);
        dto.setClosedDate(dto.isReplyReceived() ? correspondence.getRespondedDate() : correspondence.getGiveUpDate());
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
        dto.setPubZdbID(status.getPublication().getZdbID());
        dto.setStatus(status.getStatus());
        dto.setLocation(status.getLocation());
        dto.setOwner(toCuratorDTO(status.getOwner()));
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
        Collections.sort(images, new Comparator<DashboardImageBean>() {
            @Override
            public int compare(DashboardImageBean o1, DashboardImageBean o2) {
                return ObjectUtils.compare(o1.getLabel(), o2.getLabel());
            }
        });
        bean.setImages(images);
        return bean;
    }

}
