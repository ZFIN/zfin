package org.zfin.infrastructure.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.construct.ConstructComponent;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.PublicationListAdapter;
import org.zfin.publication.presentation.PublicationListBean;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/infrastructure")
public class CitationListController {

    private Logger logger = Logger.getLogger(CitationListController.class);

    @RequestMapping(value = "data-citation-list/{zdbID}")
    public String featureMarkerelationCitationList(Model model, @PathVariable String zdbID) {
        model.addAttribute("dataZdbID",zdbID);
        List<PublicationAttribution> publicationAttributions = RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(zdbID);
        List<Publication> publications = new ArrayList<>();
        for (PublicationAttribution pub : publicationAttributions) {
            publications.add(pub.getPublication());
        }
        model.addAttribute("pubCount", publications.size());
        PublicationListBean citationBean = new PublicationListAdapter(publications);
        citationBean.setOrderBy("author");
        model.addAttribute("citationList",citationBean);

        return "infrastructure/data-citation-list.page";
    }
}
