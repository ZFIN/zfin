package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Controller
@RequestMapping("/publication")
public class PublicationViewController {

    private Logger logger = Logger.getLogger(PublicationViewController.class);

    @RequestMapping("/{pubID}/orthology-list")
    public String showOrthologyList(@PathVariable String pubID,
                                    @ModelAttribute("formBean") GeneBean geneBean,
                                    Model model) {
        logger.info("zdbID: " + pubID);

        if (StringUtils.equals(pubID, "ZDB-PUB-030905-1")) {
            return "redirect:/" + pubID;
        }

        List<Marker> list = getPublicationRepository().getOrthologyGeneList(pubID);
        Publication publication = getPublicationRepository().getPublication(pubID);
        List<GeneBean> beanList = new ArrayList<>(list.size());
        for (Marker marker : list) {
            GeneBean orthologyBean = new GeneBean();
            orthologyBean.setMarker(marker);
            orthologyBean.setOrthologyPresentationBean(MarkerService.getOrthologyEvidence(marker, publication));
            beanList.add(orthologyBean);
        }
        model.addAttribute("orthologyBeanList", beanList);
        model.addAttribute("publication", publication);
        return "publication/publication-orthology-list.page";
    }

}
