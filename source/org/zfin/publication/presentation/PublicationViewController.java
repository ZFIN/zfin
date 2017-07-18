package org.zfin.publication.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.MarkerReferenceBean;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.DiseaseAnnotation;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.util.ZfinStringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Controller
public class PublicationViewController {

    private Logger logger = Logger.getLogger(PublicationViewController.class);

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private PhenotypeRepository phenotypeRepository;

    @Autowired
    private FigureViewService figureViewService;


    @RequestMapping("/publication/view/{zdbID}")
    public String view(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("abstractText", publication.getAbstractText());
        model.addAttribute("showFiguresLink", PublicationService.showFiguresLink(publication));
        model.addAttribute("curationStatusDisplay", PublicationService.getCurationStatusDisplay(publication));
        model.addAttribute("correspondenceDisplay", PublicationService.getLastAuthorCorrespondenceDisplay(publication));
        model.addAttribute("meshTermDisplayList", PublicationService.getMeshTermDisplayList(publication));
        model.addAttribute("hasCorrespondence", PublicationService.hasCorrespondence(publication));
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

        List<DiseaseAnnotation> diseaseAnnotationList = phenotypeRepository.getHumanDiseaseModels(zdbID);
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

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication));
        return "publication/publication-view.page";
    }

    @RequestMapping("/publication/view")
    public String viewByAccession(@RequestParam(value = "accession", required = false) String accession,
                                  HttpServletResponse response) {
        Publication publication = null;

        if (StringUtils.isNotBlank(accession) && StringUtils.isNumeric(accession)) {
            List<Publication> pubMedPubs = publicationRepository.getPublicationByPmid(Integer.parseInt(accession));
            if (CollectionUtils.isNotEmpty(pubMedPubs)) {
                publication = pubMedPubs.get(0);
            }
        }

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        return "redirect:/" + publication.getZdbID();
    }

    @RequestMapping("/publication/{pubID}/orthology-list")
    public String showOrthologyList(@PathVariable String pubID,
                                    @ModelAttribute("formBean") GeneBean geneBean,
                                    Model model,
                                    HttpServletResponse response) {
        logger.info("zdbID: " + pubID);

        if (StringUtils.equals(pubID, "ZDB-PUB-030905-1")) {
            return "redirect:/" + pubID;
        }

        Publication publication = getPublication(pubID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        // assumes that the orthologs are ordered by zebrafish gene
        PaginationResult<Ortholog> result = publicationRepository.getOrthologPaginationByPub(publication.getZdbID(), geneBean);
        List<Ortholog> orthologList = result.getPopulatedResults();
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
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Orthology"));
        return "publication/publication-orthology-list.page";
    }

    @RequestMapping("/publication/{pubID}/feature-list")
    public String showFeatureList(@PathVariable String pubID,
                                  @ModelAttribute("formBean") GeneBean geneBean,
                                  Model model,
                                  HttpServletResponse response) {
        logger.info("zdbID: " + pubID);

        Publication publication = getPublication(pubID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<Feature> featureList = publicationRepository.getFeaturesByPublication(publication.getZdbID());

        model.addAttribute("featureList", featureList);
        model.addAttribute("publication", publication);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Mutations and Transgenics"));
        return "feature/feature-per-publication.page";
    }

    @RequestMapping("/publication/{pubID}/fish-list")
    public String showFishList(@PathVariable String pubID,
                               @ModelAttribute("formBean") GeneBean geneBean,
                               Model model,
                               HttpServletResponse response) {
        logger.info("zdbID: " + pubID);

        Publication publication = getPublication(pubID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<Fish> featureList = publicationRepository.getFishByPublication(publication.getZdbID());

        model.addAttribute("fishList", featureList);
        model.addAttribute("publication", publication);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Fish"));
        return "fish/fish-per-publication.page";
    }


    @RequestMapping("/publication/{zdbID}/disease")
    public String disease(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("diseases", phenotypeRepository.getHumanDiseaseModels(publication.getZdbID()));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Human Disease / Zebrafish Models"));
        return "publication/publication-disease.page";
    }

    @RequestMapping("/publication/{zdbID}/genes")
    public String showGenesMarkersList(@PathVariable String zdbID,
                                       Model model,
                                       HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<Marker> markers = publicationRepository.getGenesAndMarkersByPublication(publication.getZdbID());

        if (markers.size() == 1) {
            return "redirect:/" + markers.get(0).getZdbID();
        }

        model.addAttribute("publication", publication);
        model.addAttribute("markers", markers);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Genes / Markers"));
        return "publication/publication-marker-list.page";
    }

    @RequestMapping("/publication/{zdbID}/efgs")
    public String showEFGsList(@PathVariable String zdbID,
                               Model model,
                               HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);

        MarkerType efgType = markerRepository.getMarkerTypeByName(Marker.Type.EFG.name());
        List<Marker> markers = publicationRepository.getMarkersByTypeForPublication(publication.getZdbID(), efgType);

        if (markers.size() == 1) {
            return "redirect:/" + markers.get(0).getZdbID();
        }

        model.addAttribute("markers", markers);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Engineered Foreign Genes"));
        return "publication/publication-egf-list.page";
    }

    @RequestMapping("/publication/{zdbID}/clones")
    public String showClonesList(@PathVariable String zdbID,
                                 @ModelAttribute("pagination") PaginationBean pagination,
                                 Model model,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        pagination.setMaxDisplayRecords(100);

        PaginationResult<Clone> clones = publicationRepository.getClonesByPublication(publication.getZdbID(), pagination);

        if (clones.getTotalCount() == 1) {
            return "redirect:/" + clones.getPopulatedResults().get(0).getZdbID();
        }

        pagination.setTotalRecords(clones.getTotalCount());
        pagination.setRequestUrl(request.getRequestURL());
        model.addAttribute("pagination", pagination);
        model.addAttribute("publication", publication);
        model.addAttribute("clones", clones.getPopulatedResults());
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Clones and Probes"));
        return "publication/publication-clone-list.page";
    }

    @ResponseBody
    @RequestMapping(value = "/publication/{zdbID}/genes.json", method = RequestMethod.GET)
    public List<MarkerDTO> getPublicationGenes(@PathVariable String zdbID) {
        List<Marker> genes = publicationRepository.getGenesByPublication(zdbID, false);
        List<MarkerDTO> dtos = new ArrayList<>();
        for (Marker gene : genes) {
            dtos.add(DTOConversionService.convertToMarkerDTO(gene));
        }
        return dtos;
    }

    @RequestMapping("/publication/{zdbID}/strs")
    public String showSTRList(@PathVariable String zdbID,
                                       @RequestParam("type") String type,
                                       Model model,
                                       HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        MarkerType markerType = markerRepository.getMarkerTypeByName(type);
        List<SequenceTargetingReagent> strs = publicationRepository.getSTRsByPublication(publication.getZdbID(), markerType);

        if (strs.size() == 1) {
            return "redirect:/" + strs.get(0).getZdbID();
        }

        List<STRTargetRow> rows = new ArrayList<>(strs.size());
        for (SequenceTargetingReagent str : strs) {
            for (Marker target : str.getTargetGenes()) {
                rows.add(new STRTargetRow(str, target));
            }
        }
        rows.sort(Comparator.comparing(STRTargetRow::getTarget));

        model.addAttribute("publication", publication);
        model.addAttribute("markerType", markerType);
        model.addAttribute("numSTRs", strs.size());
        model.addAttribute("rows", rows);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, markerType.getDisplayName() + " List"));
        return "publication/publication-str-list.page";
    }

    @ResponseBody
    @RequestMapping(value = "/publication/lookup", method = RequestMethod.GET)
    public List<MarkerReferenceBean> publicationLookup(@RequestParam("q") String query) {
        List<MarkerReferenceBean> results = new ArrayList<>();
        Publication publication = publicationRepository.getPublication(query);
        if (publication != null) {
            results.add(MarkerReferenceBean.convert(publication));
        }
        return results;
    }

    @RequestMapping("/publication/journal/{zdbID}")
    public String viewJournal(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Journal journal = publicationRepository.getJournalByID(zdbID);
        //try zdb_replaced data if necessary
        if (journal == null) {
            String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                journal = publicationRepository.getJournalByID(replacedZdbID);
            }
        }

        if (journal == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        journal.setPublications(publicationRepository.getPublicationForJournal(journal));

        model.addAttribute("journal", journal);

        String title = "Journal: " + journal.getAbbreviation();

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, title);

        return "publication/journal-view.page";
    }


    @RequestMapping("/publication/image-edit")
    public String getImageEdit(Model model,@RequestParam("zdbID") String zdbID) {

        Image image = publicationRepository.getImageById(zdbID);
        if (image == null) {
            return null;
        }

        model.addAttribute("image", image);

        Figure figure = image.getFigure();
        if (figure!=null) {
            Clone probe = figureViewService.getProbeForFigure(figure);
            model.addAttribute("probe", probe);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Image: " + figureViewService.getFullFigureLabel(image.getFigure()));
        }

        return "figure/image-edit.page";
    }


    @RequestMapping("/publication/printable/{zdbID}")
    public String printable (@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication.getPrintable());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        model.addAttribute("formattedDate", sdf.format(new Date()));

        return "publication/printable.ajax";
    }

    private Publication getPublication(String zdbID) {
        Publication publication = publicationRepository.getPublication(zdbID);
        if (publication == null) {
            String replacedZdbID = infrastructureRepository.getReplacedZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }
        return publication;
    }

    private String getTitle(Publication publication) {
        return getTitle(publication, null);
    }

    private String getTitle(Publication publication, String subPage) {
        String title;
        //If the mini_ref / shortAuthorList is empty, can't use it in the title...so don't!
        if (StringUtils.isEmpty(publication.getShortAuthorList())) {
            title = "Publication: " + publication.getZdbID();
        } else {
            title = ZfinStringUtils.removeHtmlTags("Publication: " + publication.getShortAuthorList());
        }
        if (StringUtils.isNotEmpty(subPage)) {
            title += ": " + subPage;
        }
        return title;
    }
}







