package org.zfin.infrastructure.presentation;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/infrastructure")
@Log4j2
public class CitationListController {

    @RequestMapping(value = "data-citation-list/{zdbID}")
    public String getCitationList(Model model, @PathVariable String zdbID) {
        return featureMarkerelationCitationList(model, zdbID, null);
    }

        @RequestMapping(value = "data-citation-list/{zdbID}/{ids}")
        public String featureMarkerelationCitationList(Model model, @PathVariable String zdbID, @PathVariable String ids) {
            model.addAttribute("dataZdbID", zdbID);
            List<Publication> publications = new ArrayList<>();
            if (ids !=null) {
                List<String> pubIDs = List.of(ids.split(","));
                for (String pubs : pubIDs) {
                    publications.add(RepositoryFactory.getPublicationRepository().getPublication(pubs));
                }
            }
            else{
                List<PublicationAttribution> publicationAttributions = RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(zdbID);
                for (PublicationAttribution pub : publicationAttributions) {
                    publications.add(pub.getPublication());
                }
            }
            model.addAttribute("pubCount", publications.size());
            PublicationListBean citationBean = new PublicationListAdapter(publications);
            citationBean.setOrderBy("author");
            model.addAttribute("citationList", citationBean);
            return "infrastructure/data-citation-list.page";
        }


}


