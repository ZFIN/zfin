package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 */
@Controller
public class PublicationMarkerViewController {

    @RequestMapping("/publication-list/{zdbID}")
    public String getPublicationList(@PathVariable String zdbID,Model model){

        List<Publication> publicationList = RepositoryFactory.getPublicationRepository().getPubsForDisplay(zdbID);
        model.addAttribute("publications",publicationList) ;


        return "marker/publication-list.page";
    }
}
