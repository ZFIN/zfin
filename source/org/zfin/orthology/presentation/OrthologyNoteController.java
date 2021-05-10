package org.zfin.orthology.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.InvalidWebRequestException;
import org.zfin.marker.Marker;
import org.zfin.marker.OrthologyNote;
import org.zfin.marker.repository.MarkerRepository;

/**
 * Add or update a note for orthology on a given marker.
 */
@RestController
@RequestMapping("/api")
@Log4j2
public class OrthologyNoteController {

    @Autowired
    private MarkerRepository markerRepository;

    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/marker/{markerZdbID}/orthology-note", method = RequestMethod.GET)
    public OrthologyNote getOrthologyNote(@PathVariable String markerZdbID) {
        Marker gene = markerRepository.getMarkerByID(markerZdbID);
        if (gene == null) {
            throw new InvalidWebRequestException("No zebrafish gene with ID " + markerZdbID + " found", null);
        }
        OrthologyNote note = gene.getOrthologyNote();
        if (note == null) {
            note = new OrthologyNote();
            note.setMarker(gene);
            note.setNote("");
        }
        return note;
    }

    @JsonView(View.OrthologyAPI.class)
    @RequestMapping(value = "/marker/{geneID}/orthology-note", method = RequestMethod.POST)
    public OrthologyNote setOrthologyNote(@PathVariable String geneID,
                                          @RequestBody OrthologyNote dto) {
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
        return note;
    }

}
