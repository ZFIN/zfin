package org.zfin.antibody.presentation;

import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.apache.log4j.Logger;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.MarkerType;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerHistory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.people.Person;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.HashMap;




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
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
            Boolean createAlias=false;

            Marker antibodytoRename = mr.getMarkerByID(formBean.getAntibody().getZdbID());
            String antibodyAlias=antibodytoRename.getAbbreviation();
            antibodytoRename.setAbbreviation(formBean.getAntibodyNewName().toLowerCase());
            antibodytoRename.setName(formBean.getAntibodyNewName());


            String pubZdbID = formBean.getAntibodyRenamePubZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            formBean.setAntibodyRenamePubZdbID(PublicationValidator.completeZdbID(pubZdbID));
            else
              formBean.setAntibodyRenamePubZdbID(pubZdbID);
            String zdbID = formBean.getAntibodyRenamePubZdbID();
            Publication antibodyPub = pr.getPublication(zdbID);


            Session session = HibernateUtil.currentSession();
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                if (formBean.isCreateAlias()) {
                    createAlias = true;
                                
                }
                        mr.updateMarker(antibodytoRename,antibodyPub,createAlias,antibodyAlias);


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



           // return new ModelAndView("antibody-detail-update.page", LookupStrings.FORM_BEAN, formBean);
            return new ModelAndView(new RedirectView("update-details?antibody.zdbID="+antibodytoRename.getZdbID()+"&antibodyDefPubZdbID="+antibodyPub.getZdbID()));
        }



    }


