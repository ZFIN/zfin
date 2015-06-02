package org.zfin.curation.service;

import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.CuratorDTO;
import org.zfin.curation.presentation.PublicationNoteDTO;
import org.zfin.profile.Person;

import java.text.SimpleDateFormat;

public class CurationDTOConversionService {

    private static final String DEFAULT_IMAGE = "/images/LOCAL/smallogo.gif";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public static PublicationNoteDTO publicationNoteToDTO(PublicationNote note) {
        PublicationNoteDTO dto = new PublicationNoteDTO();
        dto.setZdbID(note.getZdbID());
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        dto.setDate(df.format(note.getDate().getTime()));
        dto.setText(note.getText());
        dto.setCurator(personToCuratorDTO(note.getCurator()));
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

}
