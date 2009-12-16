package org.zfin.orthology.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.OrthologyNote;
import org.zfin.marker.presentation.OrthologyNoteBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.properties.ZfinProperties;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class OrthologyNoteController extends SimpleFormController {

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static Logger LOG = Logger.getLogger(OrthologyNoteController.class);

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        OrthologyNoteBean bean = (OrthologyNoteBean) command;

        if (bean.getGeneID() == null) {
            // return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
            return null;
        }
        Marker gene = markerRepository.getMarkerByID(bean.getGeneID());
        if (gene == null) {
            // return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getAnatomyItem().getZdbID());
            return null;
        }
        OrthologyNote note = gene.getOrthologyNote();
        bean.setGeneID(gene.getZdbID());
        if (note != null)
            bean.setNote(note.getNote());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, bean);
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {
        OrthologyNoteBean formBean = (OrthologyNoteBean) command;
        Marker gene = markerRepository.getMarkerByID(formBean.getGeneID());
        if (gene == null) {
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, formBean.getGeneID());
        }
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        try {
            tx = session.beginTransaction();
            markerRepository.createOrUpdateOrthologyExternalNote(gene, formBean.getNote());
            tx.commit();
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        return new ModelAndView(new RedirectView(createMarkerUpdateLink(formBean.getGeneID()), false));
    }

    private String createMarkerUpdateLink(String geneID) {
        URLCreator url = new URLCreator("/" + ZfinProperties.getWebDriver());
        url.addNamevaluePair("MIval", "aa-markerview.apg");
        url.addNamevaluePair("UPDATE", "1");
        url.addNamevaluePair("OID", geneID);
        return url.getURL(true);
    }

}
