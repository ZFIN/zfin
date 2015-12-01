package org.zfin.publication.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.feature.Feature;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.MarkerReferenceBean;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.Fish;
import org.zfin.orthology.Ortholog;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.*;

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
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
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
        model.addAttribute("abstractText", publication.getAbstractText());
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
        Long featureCount = publicationRepository.getFeatureCount(publication);
        Long fishCount = publicationRepository.getFishCount(publication);
        Long orthologyCount = publicationRepository.getOrthologyCount(publication);
        Long mappingDetailsCount = publicationRepository.getMappingDetailsCount(publication);

        model.addAttribute("markerCount", markerCount);
        model.addAttribute("morpholinoCount", morpholinoCount);
        model.addAttribute("talenCount", talenCount);
        model.addAttribute("crisprCount", crisprCount);
        model.addAttribute("antibodyCount", antibodyCount);
        model.addAttribute("efgCount", efgCount);
        model.addAttribute("cloneProbeCount", cloneProbeCount);
        model.addAttribute("expressionCount", expressionCount);
        model.addAttribute("phenotypeCount", phenotypeCount);
        model.addAttribute("featureCount", featureCount);
        //model.addAttribute("phenotypeAlleleCount", phenotypeAlleleCount);
        model.addAttribute("fishCount", fishCount);
        model.addAttribute("orthologyCount", orthologyCount);
        model.addAttribute("mappingDetailsCount", mappingDetailsCount);

        List<DiseaseAnnotation> diseaseAnnotationList = getPhenotypeRepository().getHumanDiseaseModels(zdbID);
        model.addAttribute("diseaseCount", diseaseAnnotationList.size());

        model.addAttribute("expressionAndPhenotypeLabel", PublicationService.getExpressionAndPhenotypeLabel(expressionCount, phenotypeCount));

        model.addAttribute("allowDelete", publicationRepository.canDeletePublication(publication));

        if (PublicationService.allowCuration(publication)) {
            model.addAttribute("showAdditionalData", PublicationService.hasAdditionalData(
                    markerCount, morpholinoCount,
                    talenCount, crisprCount,
                    antibodyCount, efgCount,
                    cloneProbeCount, expressionCount,
                    phenotypeCount, phenotypeAlleleCount, featureCount,
                    fishCount, orthologyCount, (long) diseaseAnnotationList.size()
            ));
        } else {
            model.addAttribute("showAdditionalData", false);
        }

        //If the mini_ref / shortAuthorList is empty, can't use it in the title...so don't!
        if (StringUtils.isEmpty(publication.getShortAuthorList())) {
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication: " + publication.getZdbID());
        } else {
            String title = "Publication: " + publication.getShortAuthorList();
            title = title.replace("<i>", "").replace("</i>", "");

            model.addAttribute(LookupStrings.DYNAMIC_TITLE, title);
        }

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

        // assumes that the orthologs are ordered by zebrafish gene
        PaginationResult<Ortholog> result = getPublicationRepository().getOrthologPaginationByPub(pubID, geneBean);
        List<Ortholog> orthologList = result.getPopulatedResults();
        Publication publication = getPublicationRepository().getPublication(pubID);
        List<GeneBean> beanList = new ArrayList<>(orthologList.size() * 4);
        List<Ortholog> orthologsPerGene = new ArrayList<>(5);
        for (int index = 0; index < orthologList.size(); index++) {
            Ortholog ortholog = orthologList.get(index);
            Marker zebrafishGene = ortholog.getZebrafishGene();
            Marker nextZebrafishGene = null;
            // if not last element set next gene
            if (index != orthologList.size() - 1) {
                nextZebrafishGene = orthologList.get(index + 1).getZebrafishGene();
            }
            orthologsPerGene.add(ortholog);

            // if the last element or the next element is a different gene
            if (nextZebrafishGene == null || !nextZebrafishGene.equals(zebrafishGene)) {
                GeneBean orthologyBean = new GeneBean();
                orthologyBean.setMarker(zebrafishGene);
                orthologyBean.setOrthologyPresentationBean(MarkerService.getOrthologyPresentationBean(orthologsPerGene, zebrafishGene, publication));
                beanList.add(orthologyBean);
            }
            if (nextZebrafishGene != null && !nextZebrafishGene.equals(zebrafishGene)) {
                orthologsPerGene = new ArrayList<>(5);
            }

        }
        geneBean.setRequestUrl(new StringBuffer("orthology-list"));
        geneBean.setTotalRecords(result.getTotalCount());
        model.addAttribute("orthologyBeanList", beanList);
        model.addAttribute("publication", publication);
        return "publication/publication-orthology-list.page";
    }

    @RequestMapping("/{pubID}/feature-list")
    public String showFeatureList(@PathVariable String pubID,
                                  @ModelAttribute("formBean") GeneBean geneBean,
                                  Model model) {
        logger.info("zdbID: " + pubID);

        List<Feature> featureList = getPublicationRepository().getFeaturesByPublication(pubID);
        Publication publication = getPublicationRepository().getPublication(pubID);

        model.addAttribute("featureList", featureList);
        model.addAttribute("publication", publication);
        return "feature/feature-per-publication.page";
    }

    @RequestMapping("/{pubID}/fish-list")
    public String showFishList(@PathVariable String pubID,
                               @ModelAttribute("formBean") GeneBean geneBean,
                               Model model) {
        logger.info("zdbID: " + pubID);

        List<Fish> featureList = getPublicationRepository().getFishByPublication(pubID);
        Publication publication = getPublicationRepository().getPublication(pubID);

        model.addAttribute("fishList", featureList);
        model.addAttribute("publication", publication);
        return "fish/fish-per-publication.page";
    }


    @RequestMapping("/{zdbID}/disease")
    public String disease(@PathVariable String zdbID, Model model, HttpServletResponse response) {

        Publication publication = getPublicationRepository().getPublication(zdbID);
        //try zdb_replaced data if necessary
        if (publication == null) {
            String replacedZdbID = getInfrastructureRepository().getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = getPublicationRepository().getPublication(replacedZdbID);
            }
        }

        //give up
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("diseases", getPhenotypeRepository().getHumanDiseaseModels(publication.getZdbID()));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Publication: " + publication.getShortAuthorList().replace("<i>", "").replace("</i> Disease", ""));

        return "publication/publication-disease.page";
    }

    @ResponseBody
    @RequestMapping(value = "{zdbID}/genes", method = RequestMethod.GET)
    public List<MarkerDTO> getPublicationGenes(@PathVariable String zdbID) {
        List<Marker> genes = getPublicationRepository().getGenesByPublication(zdbID, false);
        List<MarkerDTO> dtos = new ArrayList<>();
        for (Marker gene : genes) {
            dtos.add(DTOConversionService.convertToMarkerDTO(gene));
        }
        return dtos;
    }

    @ResponseBody
    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    public List<MarkerReferenceBean> publicationLookup(@RequestParam("q") String query) {
        List<MarkerReferenceBean> results = new ArrayList<>();
        Publication publication = getPublicationRepository().getPublication(query);
        if (publication != null) {
            results.add(MarkerReferenceBean.convert(publication));
        }
        return results;
    }
}
