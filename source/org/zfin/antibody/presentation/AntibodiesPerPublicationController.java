package org.zfin.antibody.presentation;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.throwaway.ThrowawayController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;

import java.util.List;

/**
 * Controller to retrieve all antibodies per given publication.
 */
public class AntibodiesPerPublicationController implements ThrowawayController {

    private String publicationZdbID;

    public ModelAndView execute() throws Exception {
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(publicationZdbID);
        if (publication == null )
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, publicationZdbID);

        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Antibody> antibodies = ar.getAntibodiesByPublication(publication);


        // if there is only one antibody forward to the ab detail page.
        if (antibodies.size() == 1) {
            ModelAndView modelAndView;
            AntibodyBean form = new AntibodyBean();
            Antibody antibody = antibodies.get(0);
            form.setAntibody(antibody);
            modelAndView = new ModelAndView("antibody-detail.page", LookupStrings.FORM_BEAN, form);
            modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, antibody.getName());
            return modelAndView;
        }

        ModelAndView modelAndView = new ModelAndView("antibodies-per-publication.page", LookupStrings.FORM_BEAN, antibodies);
        modelAndView.addObject(LookupStrings.DYNAMIC_TITLE, publication.getShortAuthorList());
        modelAndView.addObject("publication", publication);

        return modelAndView;
    }

    public void setPublicationZdbID(String id) {
        publicationZdbID = id;
    }
}
