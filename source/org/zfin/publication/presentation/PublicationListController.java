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
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.presentation.MarkerAliasBean;
import org.zfin.marker.presentation.MarkerRelationshipBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class PublicationListController extends MultiActionController {

    public static final String RELATIONSHIP_PUBLICATION_LIST_PAGE = "relationship-publication-list.page";
    public static final String ALIAS_PUBLICATION_LIST_PAGE = "alias-publication-list.page";
    private static Logger LOG = Logger.getLogger(PublicationListController.class);

    private Map validatorMap;

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

    public Map getValidatorMap() {
        return validatorMap;
    }

    public void setValidatorMap(Map validatorMap) {
        this.validatorMap = validatorMap;
    }

}
