package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.curation.PublicationNote;
import org.zfin.curation.presentation.PublicationNoteDTO;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;

import java.util.Collection;
import java.util.GregorianCalendar;

@Controller
@RequestMapping("/publication")
public class PublicationTrackingController {

    private final static Logger LOG = Logger.getLogger(PublicationTrackingController.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @RequestMapping(value = "/{zdbID}/track")
    public String showPubTracker(Model model, @PathVariable String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Track Pub: " + publication.getTitle());
        model.addAttribute("publication", publication);
        model.addAttribute("hasFile", StringUtils.isNotEmpty(publication.getFileName()));
        model.addAttribute("loggedInUser", ProfileService.getCurrentSecurityUser().getZdbID());
        return "publication/track-publication.page";
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/notes", method = RequestMethod.GET)
    public Collection<PublicationNoteDTO> getPublicationNotes(@PathVariable String zdbID) {
        return getPubNotes(publicationRepository.getPublication(zdbID));
    }

    @ResponseBody
    @RequestMapping(value = "/{zdbID}/notes", method = RequestMethod.POST)
    public PublicationNoteDTO addPublicationNote(@PathVariable String zdbID,
                                                             @RequestBody PublicationNoteDTO noteDTO) {
        Publication publication = publicationRepository.getPublication(zdbID);

        PublicationNote note = new PublicationNote();
        note.setText(noteDTO.getText());
        note.setDate(new GregorianCalendar());
        note.setCurator(ProfileService.getCurrentSecurityUser());
        note.setPublication(publication);

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        session.save(note);
        tx.commit();
        
        return CurationDTOConversionService.publicationNoteToDTO(note);
    }

    @ResponseBody
    @RequestMapping(value = "/notes/{zdbID}", method = RequestMethod.POST)
    public PublicationNoteDTO editPublicationNode(@PathVariable String zdbID,
                                                  @RequestBody PublicationNoteDTO noteDTO) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        PublicationNote note = (PublicationNote) session.get(PublicationNote.class, zdbID);
        note.setText(noteDTO.getText());
        session.update(note);
        tx.commit();

        return CurationDTOConversionService.publicationNoteToDTO(note);
    }

    @ResponseBody
    @RequestMapping(value = "/notes/{zdbID}", method = RequestMethod.DELETE)
    public String deletePublicationNote(@PathVariable String zdbID) {
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        PublicationNote note = (PublicationNote) session.get(PublicationNote.class, zdbID);
        session.delete(note);
        tx.commit();

        return "";
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/claims", method = RequestMethod.GET)
    public String getPublicationClaims(@PathVariable String zdbID) {
        return "";
    }

    private Collection<PublicationNoteDTO> getPubNotes(Publication publication) {
        return CollectionUtils.collect(publication.getNotes(), new Transformer() {
            @Override
            public Object transform(Object o) {
                return CurationDTOConversionService.publicationNoteToDTO((PublicationNote) o);
            }
        });
    }

}
