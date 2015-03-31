package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.service.MarkerService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

@Controller
@RequestMapping("/publication")
public class PublicationViewController {

    private Logger logger = Logger.getLogger(PublicationViewController.class);

    @RequestMapping("/view/{zdbID}")
    public String view(@PathVariable String zdbID, Model model, HttpServletResponse response) {

        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();


        Publication publication = publicationRepository.getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = RepositoryFactory.getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("abstractText", publicationRepository.getAbstractText(publication.getZdbID()));
        model.addAttribute("showFiguresLink", PublicationService.showFiguresLink(publication));
        model.addAttribute("curationStatusDisplay", PublicationService.getCurationStatusDisplay(publication));
        model.addAttribute("allowCuration", PublicationService.allowCuration(publication));

        /* counts */
        Long markerCount = publicationRepository.getMarkerCount(publication);
        Long morpholinoCount = publicationRepository.getMorpholinoCount(publication);
        Long talenCount = publicationRepository.getTalenCount(publication);
        Long crisprCount = publicationRepository.getCrisprCount(publication);
        Long antibodyCount = publicationRepository.getAntibodyCount(publication);
        Long efgCount = publicationRepository.getEfgCount(publication);
        Long cloneProbeCount = publicationRepository.getCloneProbeCount(publication);
        Long expressionCount = publicationRepository.getExpressionCount(publication);
        Long phenotypeCount = publicationRepository.getPhenotypeCount(publication);
        Long phenotypeAlleleCount = publicationRepository.getPhenotypeAlleleCount(publication);
        Long orthologyCount = publicationRepository.getOrthologyCount(publication);


        model.addAttribute("markerCount", markerCount);
        model.addAttribute("morpholinoCount", morpholinoCount);
        model.addAttribute("talenCount", talenCount);
        model.addAttribute("crisprCount", crisprCount);
        model.addAttribute("antibodyCount", antibodyCount);
        model.addAttribute("efgCount", efgCount);
        model.addAttribute("cloneProbeCount", cloneProbeCount);
        model.addAttribute("expressionCount", expressionCount);
        model.addAttribute("phenotypeCount", phenotypeCount);
        model.addAttribute("phenotypeAlleleCount", phenotypeAlleleCount);
        model.addAttribute("orthologyCount", orthologyCount);

        model.addAttribute("expressionAndPhenotypeLabel", PublicationService.getExpressionAndPhenotypeLabel(expressionCount, phenotypeCount));

        model.addAttribute("allowDelete", publicationRepository.canDeletePublication(publication));

        if (PublicationService.allowCuration(publication)) {
            model.addAttribute("showAdditionalData", PublicationService.hasAdditionalData(
                    markerCount, morpholinoCount,
                    talenCount, crisprCount,
                    antibodyCount, efgCount,
                    cloneProbeCount, expressionCount,
                    phenotypeCount, phenotypeAlleleCount,
                    orthologyCount
            ));
        } else {
            model.addAttribute("showAdditionalData", false);
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication: " + publication.getShortAuthorList().replace("<i>","").replace("</i>",""));

        return "publication/publication-view.page";
    }


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
