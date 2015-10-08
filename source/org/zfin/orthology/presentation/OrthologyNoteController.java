package org.zfin.orthology.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
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
    private static Logger LOG = Logger.getLogger(OrthologyNoteController.class);

    @ResponseBody
    @RequestMapping(value = "/gene/{geneID}/orthology-note", method = RequestMethod.GET)
    public OrthologyNoteDTO getOrthologyNote(@PathVariable String geneID) {
        Marker gene = markerRepository.getMarkerByID(geneID);
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
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        OrthologyNote note;
        try {
            tx = session.beginTransaction();
            note = markerRepository.createOrUpdateOrthologyExternalNote(gene, dto.getNote());
            tx.commit();
        } catch (Exception e) {
            try {
                if (tx != null) {
                    tx.rollback();
                }
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        if (note != null) {
            dto.setZdbID(note.getZdbID());
            dto.setGeneID(note.getMarker().getZdbID());
            dto.setNote(note.getNote());
        }
        return dto;
    }

}
