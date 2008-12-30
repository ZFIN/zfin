package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.AntibodyBean;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerAliasBean;
import org.zfin.marker.presentation.MarkerRelationshipBean;
import org.zfin.marker.presentation.SNPBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.User;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PublicationListController extends MultiActionController {

    public static final String ANTIBODY_PUBLICATION_LIST_PAGE = "antibody-publication-list.page";
    public static final String RELATIONSHIP_PUBLICATION_LIST_PAGE = "relationship-publication-list.page";
    public static final String ALIAS_PUBLICATION_LIST_PAGE = "alias-publication-list.page";
    public static final String SNP_PUBLICATION_LIST_PAGE = "snp-publication-list.page";
    private static Logger LOG = Logger.getLogger(PublicationListController.class);

    private Map validatorMap;

    public ModelAndView antibodyPublicationListHandler(HttpServletRequest request, HttpServletResponse response, AntibodyBean bean)
            throws ServletException {

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        bean.setAntibody(ab);
        return new ModelAndView(ANTIBODY_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public ModelAndView aliasPublicationListHandler(HttpServletRequest request, HttpServletResponse response, MarkerAliasBean bean)
            throws ServletException {

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        MarkerAlias alias = infrastructureRepository.getMarkerAliasByID(bean.getMarkerAlias().getZdbID());
        bean.setMarkerAlias(alias);
        return new ModelAndView(ALIAS_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public ModelAndView relationshipPublicationListHandler(HttpServletRequest request, HttpServletResponse response, MarkerRelationshipBean bean)
            throws ServletException {

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        MarkerRelationship markerRelationship = markerRepository.getMarkerRelationshipByID(bean.getMarkerRelationship().getZdbID());
        bean.setMarkerRelationship(markerRelationship);
        return new ModelAndView(RELATIONSHIP_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public ModelAndView snpPublicationListHandler(HttpServletRequest request, HttpServletResponse response, SNPBean bean)
            throws ServletException {

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        Marker marker = markerRepository.getMarkerByID(bean.getMarkerID());
        bean.setMarker(marker);
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        List<String> pubIDs = publicationRepository.getSNPPublicationIDs(bean.getMarker());
        Set<Publication> pubs = new HashSet<Publication>();
        for (String id : pubIDs) {
            pubs.add(publicationRepository.getPublication(id));
        }
        bean.setPublications(pubs);
        return new ModelAndView(SNP_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public ModelAndView disassociatePublicationListHandler(HttpServletRequest request, HttpServletResponse response, AntibodyBean bean)
            throws ServletException {
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            ir.removeRecordAttributionForData(bean.getDisassociatedPubId(), ab.getZdbID());
            User currentUser = User.getCurrentSecurityUser();
            ir.insertUpdatesTable(ab, "antibody attribution", "", currentUser, "", "");
            tx.commit();
        } catch (Exception exception) {
            try {
                tx.rollback();
            } catch (HibernateException hibernateException) {
                LOG.error("Error during roll back of transaction", hibernateException);
            }
            LOG.error("Error in Transaction", exception);
            throw new RuntimeException("Error during transaction. Rolled back.", exception);
        }

        bean.setAntibody(ab);

        return new ModelAndView(ANTIBODY_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public ModelAndView associatePublicationHandler(HttpServletRequest request, HttpServletResponse response, AntibodyBean bean)
            throws ServletException {
        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        User currentUser = User.getCurrentSecurityUser();

        String pubID = bean.getAntibodyNewPubZdbID();
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(pubID);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            markerRepository.addMarkerPub(ab, publication);
            ir.insertUpdatesTable(ab, "antibody attribution", "", currentUser, "", "");
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

        bean.setAntibody(ab);
        bean.setAntibodyNewPubZdbID("");

        return new ModelAndView(ANTIBODY_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    public Map getValidatorMap() {
        return validatorMap;
    }

    public void setValidatorMap(Map validatorMap) {
        this.validatorMap = validatorMap;
    }

    private ModelAndView validate(HttpServletRequest request, AntibodyBean bean) {
        BindException errors = new BindException(bean, LookupStrings.FORM_BEAN);
        String requestURI = request.getRequestURI();
        int lastSlash = requestURI.lastIndexOf("/");
        requestURI = requestURI.substring(lastSlash + 1);

        if (getValidatorMap() != null) {
            Validator validator = (Validator) getValidatorMap().get(requestURI);
            ValidationUtils.invokeValidator(validator, bean, errors);
        }

        // if pub zdb ID is short version, complete the zdb ID
        String pubZdbID = bean.getAntibodyNewPubZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            bean.setAntibodyNewPubZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
            bean.setAntibodyNewPubZdbID(pubZdbID);

        BindingResult result = errors.getBindingResult();
        if (!result.hasErrors())
            return null;
        Map model = result.getModel();

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        bean.setAntibody(ab);

        ModelAndView view = new ModelAndView(ANTIBODY_PUBLICATION_LIST_PAGE, LookupStrings.FORM_BEAN, bean);

        view.addAllObjects(model);

        return view;
    }
}