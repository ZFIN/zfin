package org.zfin.curation.service;

import org.zfin.curation.Curation;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.CurationDTO;
import org.zfin.curation.presentation.CuratorDTO;
import org.zfin.curation.presentation.PublicationNoteDTO;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

public class CurationDTOConversionService {

    private static final String DEFAULT_IMAGE = "/images/LOCAL/smallogo.gif";

    public static PublicationNoteDTO publicationNoteToDTO(PublicationNote note) {
        PublicationNoteDTO dto = new PublicationNoteDTO();
        dto.setZdbID(note.getZdbID());
        dto.setDate(note.getDate());
        dto.setText(note.getText());
        dto.setCurator(personToCuratorDTO(note.getCurator()));

        Person currentUser = ProfileService.getCurrentSecurityUser();
        dto.setEditable(currentUser != null && currentUser.equals(note.getCurator()));

        return dto;
    }

    public static CuratorDTO personToCuratorDTO(Person curator) {
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

    public static CurationDTO curationToDTO(Curation curation) {
        CurationDTO dto = new CurationDTO();
        dto.setZdbID(curation.getZdbID());
        dto.setTopic(curation.getTopic().toString());
        dto.setCurator(personToCuratorDTO(curation.getCurator()));
        dto.setDataFound(curation.isDataFound());
        dto.setEntryDate(curation.getEntryDate());
        dto.setOpenedDate(curation.getOpenedDate());
        dto.setClosedDate(curation.getClosedDate());
        return dto;
    }

}
