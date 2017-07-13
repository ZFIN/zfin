package org.zfin.marker.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
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
        citationBean.setOrderBy("author");
        model.addAttribute("citationList",citationBean);

        return "marker/citation-list.page";
    }

    @RequestMapping("/go-citation-list/markerID/{markerZdbID}/mrkrGoEvdTermZdbID/{markerGoEvdTermZdbID}/evidenceCode/{evdCode}/inference/{inf}")
    public String getPublicationListForMarkerGoEvd(@PathVariable String markerZdbID, @PathVariable String markerGoEvdTermZdbID, @PathVariable String evdCode, @PathVariable String inf, Model model) {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID(markerZdbID);
        model.addAttribute("marker",marker);
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        List<String> publicationIDs = publicationRepository.getPublicationIdsForMarkerGo(markerZdbID,markerGoEvdTermZdbID,evdCode,inf);
        List<Publication> publications = new ArrayList<>();
        for (String pubID : publicationIDs) {
            publications.add(publicationRepository.getPublication(pubID));
        }
        model.addAttribute("pubCount", publications.size());
        PublicationListBean citationBean = new PublicationListAdapter(publications);
        citationBean.setOrderBy("author");
        model.addAttribute("citationList",citationBean);

        return "marker/go-citation-list.page";
    }

}
