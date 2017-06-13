package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 */
@Controller
@RequestMapping("/marker")
public class PublicationMarkerViewController {

    @RequestMapping("/citation-list/{zdbID}")
    public String getPublicationList(@PathVariable String zdbID,Model model){
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID);
        model.addAttribute("marker",marker);
        List<Publication> publications = RepositoryFactory.getPublicationRepository().getPubsForDisplay(zdbID);
        model.addAttribute("pubCount",publications.size());
        PublicationListBean citationBean = new PublicationListAdapter(publications);
        model.addAttribute("citationList",citationBean);

        return "marker/citation-list.page";
    }
}
