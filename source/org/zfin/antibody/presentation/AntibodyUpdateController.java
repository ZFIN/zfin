package org.zfin.antibody.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


public class AntibodyUpdateController extends SimpleFormController {

    private static Logger LOG = Logger.getLogger(AntibodyUpdateController.class);

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
        UpdateAntibodyFormBean formBean = (UpdateAntibodyFormBean) command;
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibody = antibodyRepository.getAntibodyByID(formBean.getAntibody().getZdbID());
        formBean.setAntibody(antibody);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, formBean);
        map.put(LookupStrings.DYNAMIC_TITLE, antibody.getName());
        return map;
    }

    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        UpdateAntibodyFormBean formBean = (UpdateAntibodyFormBean) command;

        String pubZdbID = formBean.getAntibodyRenamePubZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setAntibodyRenamePubZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setAntibodyRenamePubZdbID(pubZdbID);
        String zdbID = formBean.getAntibodyRenamePubZdbID();

        String pubID;
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        Antibody antibody;
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            Publication antibodyPub = pr.getPublication(zdbID);
            pubID = antibodyPub.getZdbID();

            antibody = ar.getAntibodyByID(formBean.getAntibody().getZdbID());
            String antibodyAlias = antibody.getName();
            antibody.setAbbreviation(formBean.getAntibodyNewName().toLowerCase());
            antibody.setName(formBean.getAntibodyNewName());

            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            if (formBean.isCreateAlias())
                mr.updateMarker(antibody, antibodyPub, antibodyAlias);
            else
                mr.updateMarker(antibody, antibodyPub, null);
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

        return new ModelAndView(new RedirectView("update-details?antibody.zdbID=" + antibody.getZdbID() + "&antibodyDefPubZdbID=" + pubID));
    }


}


