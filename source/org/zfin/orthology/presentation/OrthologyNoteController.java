package org.zfin.orthology.presentation;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.Marker;
import org.zfin.marker.OrthologyNote;
import org.zfin.marker.presentation.OrthologyNoteDTO;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Add or update a note for orthology on a given marker.
 */
@Controller
public class OrthologyNoteController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static Logger LOG = LogManager.getLogger(OrthologyNoteController.class);

    @ResponseBody
    @RequestMapping(value = "/gene/{geneID}/orthology-note", method = RequestMethod.GET)
    public OrthologyNoteDTO getOrthologyNote(@PathVariable String geneID) {
        Marker gene = markerRepository.getMarkerByID(geneID);
        if (gene == null) {
            throw new InvalidWebRequestException("No zebrafish gene with ID " + geneID + " found", null);
        }
        OrthologyNote note = gene.getOrthologyNote();
        OrthologyNoteDTO dto = new OrthologyNoteDTO();
        if (note != null) {
            dto.setZdbID(note.getZdbID());
            dto.setGeneID(note.getExternalDataZdbID());
            dto.setNote(note.getNote());
        }
        return dto;
    }

    @ResponseBody
    @RequestMapping(value = "/gene/{geneID}/orthology-note", method = RequestMethod.POST)
    public OrthologyNoteDTO setOrthologyNote(@PathVariable String geneID,
                                             @RequestBody OrthologyNoteDTO dto) {
        Marker gene = markerRepository.getMarkerByID(geneID);
        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        OrthologyNote note;
        try {
            note = markerRepository.createOrUpdateOrthologyExternalNote(gene, dto.getNote());
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw new InvalidWebRequestException("Couldn't save note", null);
        }
        if (note != null) {
            dto.setZdbID(note.getZdbID());
            dto.setGeneID(note.getMarker().getZdbID());
            dto.setNote(note.getNote());
        }
        return dto;
    }

}
