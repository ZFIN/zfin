package org.zfin.publication.presentation;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller that serves the people pages.
 */
public class ViewPublicationDetailController extends AbstractCommandController {

    private static Logger LOG = Logger.getLogger(ViewPublicationDetailController.class);
    private PublicationRepository publicationRep = RepositoryFactory.getPublicationRepository();

    public ViewPublicationDetailController() {
        setCommandClass(PublicationBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
                                  BindException errors) throws Exception {

        PublicationBean publicationForm = (PublicationBean) command;
        Publication publication = publicationForm.getPublication();
        ViewPublicationDetailController.LOG.info("Start Action Class");
        publication = publicationRep.getPublication(publication.getZdbID());
        publicationForm.setPublication(publication);
        return new ModelAndView("view-publication-detail", LookupStrings.FORM_BEAN, publicationForm);
    }

}
