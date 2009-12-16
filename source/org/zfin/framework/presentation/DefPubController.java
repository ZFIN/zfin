package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationBean;
import org.zfin.publication.presentation.PublicationValidator;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller to populate the Def-Pub section.
 */
public class DefPubController extends AbstractCommandController {

    public DefPubController() {
        setCommandClass(PublicationBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        PublicationBean bean = (PublicationBean) command;
        String pubID = bean.getZdbID();
        if (StringUtils.isEmpty(pubID))
            return new ModelAndView("def-pub.page", LookupStrings.FORM_BEAN, bean);

        PublicationValidator.validatePublicationID(pubID, "zdbID", errors);
        if (errors.hasErrors())
            return new ModelAndView("def-pub.page", LookupStrings.FORM_BEAN, bean);

        pubID = pubID.trim();
        if (PublicationValidator.isShortVersion(pubID))
            bean.setZdbID(PublicationValidator.completeZdbID(pubID));
        else
            bean.setZdbID(pubID);

        PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
        Publication publication = pubRepository.getPublication(bean.getZdbID());
        if (publication != null) {
            bean.setValidPublication(true);
            bean.setPublication(publication);
        } else {
            bean.setZdbID(pubID);
        }

        return new ModelAndView("def-pub.page", LookupStrings.FORM_BEAN, bean);
    }
}
