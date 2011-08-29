package org.zfin.antibody.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.presentation.AntibodyMarkerBean;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller to retrieve all antibodies per given publication.
 */
public class AntibodiesPerPublicationController implements Controller {


    @Override
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        String publicationZdbID = httpServletRequest.getParameter("publicationZdbID");
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(publicationZdbID);
        if (publication == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, publicationZdbID);

        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Antibody> antibodies = ar.getAntibodiesByPublication(publication);


        // if there is only one antibody forward to the ab detail page.
        if (antibodies.size() == 1) {
            Antibody antibody = antibodies.get(0);
            return new ModelAndView(new RedirectView("/action/marker/view/" + antibody.getZdbID()));
        }

        ModelAndView modelAndView = new ModelAndView("antibodies-per-publication.page", LookupStrings.FORM_BEAN, antibodies);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, publication.getShortAuthorList());
        modelAndView.addObject("publication", publication);

        return modelAndView;
    }

}
