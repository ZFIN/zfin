package org.zfin.orthology.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.OrthologyNote;
import org.zfin.marker.presentation.OrthologyNoteBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletResponse;

/**
 * Add or update a note for orthology on a given marker.
 */
@Controller
@RequestMapping("/orthology")
public class OrthologyNoteController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static Logger LOG = Logger.getLogger(OrthologyNoteController.class);

    @RequestMapping("/save-note/{zdbID}")
    public String updateOrthologyNote(@PathVariable String zdbID,
                                      @ModelAttribute("formBean") OrthologyNoteBean formBean,
                                      Model model,
                                      HttpServletResponse response) throws Exception {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return "record-not-found.page";
        }
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        try {
            tx = session.beginTransaction();
            markerRepository.createOrUpdateOrthologyExternalNote(gene, formBean.getNote());
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
        response.sendRedirect(createMarkerUpdateLink(zdbID));
        return null;
    }

    @RequestMapping("/view-note-form/{zdbID}")
    public String createOrthologyNote(@PathVariable String zdbID,
                                      @ModelAttribute("formBean") OrthologyNoteBean formBean,
                                      Model model) throws Exception {
        Marker gene = markerRepository.getMarkerByID(zdbID);
        if (gene == null) {
            model.addAttribute(LookupStrings.ZDB_ID, zdbID);
            return "record-not-found.page";
        }
        formBean.setGeneID(zdbID);

        OrthologyNote note = gene.getOrthologyNote();
        if (note != null)
            formBean.setNote(note.getNote());
        return "create-note.page";
    }

    private String createMarkerUpdateLink(String geneID) {
        URLCreator url = new URLCreator("/" + ZfinProperties.getWebDriver());
        url.addNamevaluePair("MIval", "aa-markerview.apg");
        url.addNamevaluePair("UPDATE", "1");
        url.addNamevaluePair("OID", geneID);
        return url.getURL();
    }

}
