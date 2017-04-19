package org.zfin.antibody.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.repository.AntibodyRepository;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Controller to retrieve all antibodies per given publication.
 */
@Controller
@RequestMapping("/antibody")
public class AntibodiesPerPublicationController {


    @RequestMapping("/antibodies-per-publication/{id}")
    public String showAntibodies(Model model,
                                 @PathVariable("id") String publicationID) throws Exception {

        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication publication = pr.getPublication(publicationID);
        if (publication == null)
            return "record-not-found.page";

        AntibodyRepository ar = RepositoryFactory.getAntibodyRepository();
        List<Antibody> antibodies = ar.getAntibodiesByPublication(publication);


        // if there is only one antibody forward to the ab detail page.
        if (antibodies.size() == 1) {
            Antibody antibody = antibodies.get(0);
            return "redirect:/" + antibody.getZdbID();
        }
        model.addAttribute("publication", publication);
        model.addAttribute(LookupStrings.FORM_BEAN, antibodies);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "antibody list for " + publication.getShortAuthorList().replaceAll("<[^>]+>", ""));

        return "antibody/antibodies-per-publication.page";
    }

}
