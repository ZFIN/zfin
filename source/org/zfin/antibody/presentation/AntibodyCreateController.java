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
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.User;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


public class AntibodyCreateController extends SimpleFormController {
    private static Logger LOG = Logger.getLogger(AntibodyCreateController.class);

    protected Map referenceData(HttpServletRequest request, Object command, Errors errors) {
       CreateAntibodyFormBean formBean = (CreateAntibodyFormBean) command;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(LookupStrings.FORM_BEAN, formBean);
        return map;       
    }
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
                                    Object command, BindException errors) throws Exception {

        CreateAntibodyFormBean formBean = (CreateAntibodyFormBean) command;
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

        User currentUser = User.getCurrentSecurityUser();

        Antibody newAntibody = new Antibody();
        newAntibody.setAbbreviation(formBean.getAntibodyName().toLowerCase());
        newAntibody.setName(formBean.getAntibodyName());
        newAntibody.setOwner(currentUser);

        String pubZdbID = formBean.getAntibodyPublicationZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setAntibodyPublicationZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            formBean.setAntibodyPublicationZdbID(pubZdbID);
        String zdbID = formBean.getAntibodyPublicationZdbID();
        Publication antibodyPub = pr.getPublication(zdbID);


        MarkerType mt = mr.getMarkerTypeByName(Marker.Type.ATB.toString());
        newAntibody.setMarkerType(mt);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            mr.createMarker(newAntibody, antibodyPub);
            ir.insertUpdatesTable(newAntibody, "new Antibody", "", currentUser,newAntibody.getAbbreviation(),"");

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



     //   return new ModelAndView("antibody-detail-update.page", LookupStrings.FORM_BEAN, formBean);

        return new ModelAndView(new RedirectView("update-details?antibody.zdbID="+newAntibody.getZdbID()+"&antibodyDefPubZdbID="+antibodyPub.getZdbID()));
    }


}



