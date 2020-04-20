package org.zfin.infrastructure.presentation;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
import java.util.Optional;

import static org.zfin.repository.RepositoryFactory.getConstructRepository;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

@Controller
@RequestMapping("/infrastructure")
public class CitationListController {

    private Logger logger = LogManager.getLogger(CitationListController.class);

    @RequestMapping(value = "data-citation-list/{zdbID}/{ids}")
    public String featureMarkerelationCitationList(Model model, @PathVariable String zdbID, @PathVariable String ids) {
        model.addAttribute("dataZdbID", zdbID);
        List<Publication> publications = new ArrayList<>();
        if (ids != null) {
            List<String> pubIDs = Arrays.asList(ids.split(","));

            for (String pubs : pubIDs) {
                publications.add(RepositoryFactory.getPublicationRepository().getPublication(pubs));

            }
            model.addAttribute("pubCount", pubIDs.size());

        } else

        {

            List<PublicationAttribution> publicationAttributions = RepositoryFactory.getInfrastructureRepository().getPublicationAttributions(zdbID);

            for (PublicationAttribution pub : publicationAttributions) {
                publications.add(pub.getPublication());
            }
            model.addAttribute("pubCount", publications.size());
        }

        PublicationListBean citationBean = new PublicationListAdapter(publications);
        citationBean.setOrderBy("author");
        model.addAttribute("citationList", citationBean);

        return "infrastructure/data-citation-list.page";
    }
}

