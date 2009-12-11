package org.zfin.antibody.presentation;

import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.validation.*;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.HibernateUtil;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.MarkerAlias;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.people.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.infrastructure.DataAlias;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Controller that updates antibody attributes.
 */
public class AntibodyUpdateDetailController extends MultiActionController {

    public static final String UPDATE_DETAIL_PAGE = "antibody-detail-update.page";
    private static Logger LOG = Logger.getLogger(AntibodyUpdateController.class);
    private AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
    private Map validatorMap;
    private static final String MOST_RECENT_PUBLICATIONS = "MostRecentPublications";

    public ModelAndView updateDetailsHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        bindRequestParameterToFormBean(request, bean);
        Antibody ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        if (ab == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, bean.getAntibody().getZdbID());

        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());
        return antibodyData(bean);
    }

    /**
     * This method retrieves the antibody data for the antibody update page.
     *
     * @param bean AntibodyUpdateDetailBean
     * @return Model and View
     */
    private ModelAndView antibodyData(AntibodyUpdateDetailBean bean) {

        addAntibodyDataToFormBean(bean);
        return new ModelAndView(UPDATE_DETAIL_PAGE, LookupStrings.FORM_BEAN, bean);
    }

    private void addAntibodyDataToFormBean(AntibodyUpdateDetailBean bean) {
        Antibody ab;
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        ab = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        bean.setAntibody(ab);
    }

    private void bindRequestParameterToFormBean(HttpServletRequest request, AntibodyUpdateDetailBean bean) {
        ServletRequestDataBinder binder = new ServletRequestDataBinder(bean);
        binder.bind(request);
    }


    public ModelAndView updateAbPropertiesHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Person currentUser = Person.getCurrentSecurityUser();
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            if (StringUtils.isEmpty(bean.getAntibody().getClonalType())) {
                bean.getAntibody().setClonalType(null);
            }
            antibodytoUpdate.setClonalType(bean.getAntibody().getClonalType());
            ir.insertUpdatesTable(antibodytoUpdate, "clonal type", "", currentUser);

            if (StringUtils.isEmpty(bean.getAntibody().getLightChainIsotype())) {
                bean.getAntibody().setLightChainIsotype(null);
            }
            antibodytoUpdate.setLightChainIsotype(bean.getAntibody().getLightChainIsotype());
            ir.insertUpdatesTable(antibodytoUpdate, "light chain isotype", "", currentUser);

            if (StringUtils.isEmpty(bean.getAntibody().getHeavyChainIsotype())) {
                bean.getAntibody().setHeavyChainIsotype(null);
            }
            antibodytoUpdate.setHeavyChainIsotype(bean.getAntibody().getHeavyChainIsotype());
            ir.insertUpdatesTable(antibodytoUpdate, "heavy chain isotype", "", currentUser);

            if (StringUtils.isEmpty(bean.getAntibody().getImmunogenSpecies())) {
                bean.getAntibody().setImmunogenSpecies(null);
            }
            antibodytoUpdate.setImmunogenSpecies(bean.getAntibody().getImmunogenSpecies());
            ir.insertUpdatesTable(antibodytoUpdate, "immunogenspecies", "", currentUser);

            if (StringUtils.isEmpty(bean.getAntibody().getHostSpecies())) {
                bean.getAntibody().setHostSpecies(null);
            }
            antibodytoUpdate.setHostSpecies(bean.getAntibody().getHostSpecies());
            ir.insertUpdatesTable(antibodytoUpdate, "host species", "", currentUser);

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
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());
        return antibodyData(bean);
    }

    public ModelAndView addAliasHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodyUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Person currentUser = Person.getCurrentSecurityUser();
        String pubID = bean.getAntibodyDefPubZdbID();
        if (bean.getAttribution().equals("")) {
            pubID = bean.getAntibodyDefPubZdbID();
        }

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            Publication publication = pr.getPublication(pubID);
            mr.addMarkerAlias(antibodyUpdate, bean.getNewAlias(), publication);

            ir.insertUpdatesTable(antibodyUpdate, "alias", "", currentUser);
            tx.commit();
            bean.setNewAlias("");
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        bean.setAntibody(antibodyUpdate);
        return antibodyData(bean);
    }


    public ModelAndView addAliasAttribHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Person currentUser = Person.getCurrentSecurityUser();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        DataAlias alias = markerRepository.getMarkerAlias(bean.getAntibodyAliaszdbID());
        String pubZdbID = bean.getAntibodyDefPubZdbID();
        Publication pub = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            markerRepository.addDataAliasAttribution(alias, pub, antibodytoUpdate);
            ir.insertUpdatesTable(antibodytoUpdate, "alias attribution", "", currentUser);
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

        bean.setAntibody(antibodytoUpdate);
        return antibodyData(bean);
    }

    public ModelAndView addAntigenHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;


        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Marker antigenGene = mr.getMarker(mr.getMarkerByAbbreviation(bean.getNewAntigenGene()));
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Person currentUser = Person.getCurrentSecurityUser();
        String pub = bean.getAntibodyDefPubZdbID();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            MarkerRelationship mrel = new MarkerRelationship();
            mrel.setType(MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
            mrel.setFirstMarker(antigenGene);
            mrel.setSecondMarker(antibodytoUpdate);
            mr.addMarkerRelationship(mrel, pub);
            tx.commit();
            bean.setNewAntigenGene("");
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        bean.setAntibody(antibodytoUpdate);
        return antibodyData(bean);
    }

    private ModelAndView validate(HttpServletRequest request, AntibodyUpdateDetailBean bean) {
        BindException errors = new BindException(bean, LookupStrings.FORM_BEAN);
        String requestURI = request.getRequestURI();
        int lastSlash = requestURI.lastIndexOf("/");
        requestURI = requestURI.substring(lastSlash + 1);

        if (getValidatorMap() != null) {
            Validator validator = (Validator) getValidatorMap().get(requestURI);
            ValidationUtils.invokeValidator(validator, bean, errors);
        }
        // if pub zdb ID is short version, complete the zdb ID
        String pubZdbID = bean.getAntibodyDefPubZdbID().trim();
        if (PublicationValidator.isShortVersion(pubZdbID))
            bean.setAntibodyDefPubZdbID(PublicationValidator.completeZdbID(pubZdbID));
        else
             bean.setAntibodyDefPubZdbID(pubZdbID);
        String pubID = bean.getAntibodyDefPubZdbID();

        addPubIdToPublicationList(request, bean, pubID);
        BindingResult result = errors.getBindingResult();
        if (!result.hasErrors())
            return null;
        Map model = result.getModel();
        ModelAndView view = antibodyData(bean);
        view.addAllObjects(model);

        return view;
    }

    @SuppressWarnings("unchecked")
    private void addPubIdToPublicationList(HttpServletRequest request, AntibodyUpdateDetailBean bean, String pubID) {

        List<Publication> mostRecentsPubs = (List<Publication>) request.getSession().getAttribute(MOST_RECENT_PUBLICATIONS);
        if (mostRecentsPubs == null)
            mostRecentsPubs = new ArrayList<Publication>();
        if (StringUtils.isNotEmpty(pubID)) {
            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            mostRecentsPubs.add(pr.getPublication(pubID));
            request.getSession().setAttribute(MOST_RECENT_PUBLICATIONS, mostRecentsPubs);
        }
        bean.setMostRecentPubs(mostRecentsPubs);
        bean.setAttribution(pubID);
    }

    public ModelAndView deleteAliasHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        String abAlias = bean.getAntibodyAliaszdbID();
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());

        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Antibody antibody = ar.getAntibodyByID(bean.getAntibody().getZdbID());
            MarkerAlias alias = mr.getMarkerAlias(abAlias);
            mr.deleteMarkerAlias(antibody, alias);
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
        return antibodyData(bean);
    }

    public ModelAndView deleteAliasRefHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        String aliasRef = bean.getAliasRef();
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ir.removeRecordAttributionForData(aliasRef, bean.getAntibodyAliaszdbID());
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
        return antibodyData(bean);
    }

    public ModelAndView deleteAntigenHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        String relID = bean.getAntibodyAntigenzdbID();
        MarkerRelationship mrel = markerRepository.getMarkerRelationshipByID(relID);       
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            markerRepository.deleteMarkerRelationship(mrel);
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
        return antibodyData(bean);
    }

    public ModelAndView deleteAntigenRefHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        String antigenRef = bean.getAntigenRef();
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            ir.removeRecordAttributionForData(antigenRef, bean.getAntibodyAntigenzdbID());
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
        return antibodyData(bean);
    }

    public ModelAndView addAntigenAttribHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        //Marker antigenGene = mr.getMarker(mr.getMarkerByID(bean.getAttribAntigen()));
        // MarkerRelationship mrel = mr.getSpecificMarkerRelationship(antibodytoUpdate, antigenGene, MarkerRelationship.Type.ANTIBODY_RECOGNIZES_PRODUCTS_OF_GENE);
        MarkerRelationship mrel = mr.getMarkerRelationshipByID(bean.getAntibodyAntigenzdbID());
        String pubZdbID = bean.getAntibodyDefPubZdbID();
        if (bean.getAttribution().equals("")) {
            pubZdbID = bean.getAntibodyDefPubZdbID();
        }

        Session session = HibernateUtil.currentSession();

        Publication pub = RepositoryFactory.getPublicationRepository().getPublication(pubZdbID);

        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            mr.addMarkerRelationshipAttribution(mrel, pub, antibodytoUpdate);
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
        bean.setAntibody(antibodytoUpdate);
        return antibodyData(bean);
    }

    public ModelAndView addSupplierHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;
        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
        Organization or = profileRepository.getOrganizationByName(bean.getSupplierName());
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;

        try {

            tx = session.beginTransaction();
            MarkerSupplier supplier = new MarkerSupplier();
            supplier.setOrganization(or);
            supplier.setMarker(bean.getAntibody());
            session.save(supplier);
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
        return antibodyData(bean);
    }

    public ModelAndView deleteSupplierHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        ProfileRepository pr = RepositoryFactory.getProfileRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        String supplierzdbid = bean.getSupplierzdbID();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        Organization org = pr.getOrganizationByID(supplierzdbid);
        MarkerSupplier supplier = pr.getSpecificSupplier(bean.getAntibody(), org);
        Person currentUser = Person.getCurrentSecurityUser();
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            pr.deleteSupplier(supplier);
            ir.insertUpdatesTable(antibodytoUpdate, "deleted supplier", "", currentUser);
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
        return antibodyData(bean);
    }

    public ModelAndView addNoteHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        ModelAndView errorView = validate(request, bean);
        if (errorView != null)
            return errorView;
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Antibody antibodyToUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        Person currentUser = Person.getCurrentSecurityUser();
        Session session = HibernateUtil.currentSession();
        String pub = bean.getAntibodyDefPubZdbID();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            mr.addAntibodyExternalNote(antibodyToUpdate, bean.getNewNote(), pub);
            ir.insertUpdatesTable(antibodyToUpdate, "notes", "", currentUser);
            tx.commit();
            bean.setNewNote("");
        } catch (Exception e) {
            try {
                tx.rollback();
            } catch (HibernateException he) {
                LOG.error("Error during roll back of transaction", he);
            }
            LOG.error("Error in Transaction", e);
            throw new RuntimeException("Error during transaction. Rolled back.", e);
        }
        bean.setAntibody(antibodyToUpdate);
        return antibodyData(bean);
    }

    public ModelAndView editNoteHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {
        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());
        Person currentUser = Person.getCurrentSecurityUser();
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());
        String note = bean.getUsageNote()[0];
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            mr.editAntibodyExternalNote(bean.getAntibodyNotezdbID(), note);
            ir.insertUpdatesTable(antibodytoUpdate, "updated notes", "", currentUser);
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
        bean.setAntibody(antibodytoUpdate);
        return antibodyData(bean);
    }

    public ModelAndView deleteNoteHandler(HttpServletRequest request, HttpServletResponse response, AntibodyUpdateDetailBean bean)
            throws ServletException {

        addPubIdToPublicationList(request, bean, bean.getAntibodyDefPubZdbID());
        Person currentUser = Person.getCurrentSecurityUser();
        AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
        Antibody antibodytoUpdate = antibodyRepository.getAntibodyByID(bean.getAntibody().getZdbID());

        String noteID = bean.getAntibodyNotezdbID();

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
            ir.deleteActiveDataByZdbID(noteID);
            ir.insertUpdatesTable(antibodytoUpdate, "deleted notes", "", currentUser);
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
        return antibodyData(bean);
    }

    public Map getValidatorMap() {
        return validatorMap;
    }

    public void setValidatorMap(Map validatorMap) {
        this.validatorMap = validatorMap;
    }
}
