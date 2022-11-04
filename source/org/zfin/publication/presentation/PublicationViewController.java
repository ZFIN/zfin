package org.zfin.publication.presentation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.presentation.ImageResult;
import org.zfin.feature.Feature;
import org.zfin.figure.presentation.FigureExpressionSummary;
import org.zfin.figure.presentation.FigurePhenotypeSummary;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.ComparatorCreator;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.MarkerReferenceBean;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.PhenotypeRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.service.RelatedDataService;
import org.zfin.util.ZfinStringUtils;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;
import org.zfin.zebrashare.repository.ZebrashareRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringEscapeUtils.escapeXml;
import static org.zfin.repository.RepositoryFactory.getFigureRepository;
import static org.zfin.util.ZfinStringUtils.objectToJson;

@Controller
@RequestMapping("/publication")
public class PublicationViewController {

    private Logger logger = LogManager.getLogger(PublicationViewController.class);

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

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ZebrashareRepository zebrashareRepository;

    @Autowired
    private RelatedDataService relatedDataService;

    @RequestMapping("/{zdbID}")
    public String viewPublication(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            String replacedZdbID = infrastructureRepository.getWithdrawnZdbID(zdbID);
            if (replacedZdbID != null) {
                publication = publicationRepository.getPublication(replacedZdbID);
            }
        }

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("abstractText", publication.getAbstractText());
        model.addAttribute("curationStatusDisplay", publicationService.getCurationStatusDisplay(publication));
        model.addAttribute("correspondenceDisplay", publicationService.getLastAuthorCorrespondenceDisplay(publication));
        model.addAttribute("meshTermDisplayList", publicationService.getMeshTermDisplayList(publication));
        model.addAttribute("hasCorrespondence", publicationService.hasCorrespondence(publication));
        model.addAttribute("allowCuration", publicationService.allowCuration(publication));
        model.addAttribute("allowDelete", publicationRepository.canDeletePublication(publication));

        ZebrashareSubmissionMetadata zebraShareMetadata = zebrashareRepository.getZebraShareSubmissionMetadataForPublication(publication);
        if (zebraShareMetadata != null) {
            model.addAttribute("zebraShareMetadata", zebraShareMetadata);
            model.addAttribute("zebraShareEditors", zebrashareRepository.getZebraShareEditorsForPublication(publication));
            model.addAttribute("zebraShareFigures", publicationRepository.getFiguresByPublication(publication.getZdbID()));
        }
        List<ImageResult> images = publicationService.getImageResults(publication);
        model.addAttribute("imageResults", images);

        model.addAttribute("imagesJson", escapeXml(objectToJson(images)));

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication));
        model.addAttribute("relatedData", relatedDataService.getXrefsLinks(publication.getZdbID(), "Publication", null));

        return "publication/publication-view";
    }

    @RequestMapping("/view")
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

    @RequestMapping("/{pubID}/orthology-list")
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
        return "publication/publication-orthology-list";
    }

    @RequestMapping("/{pubID}/feature-list")
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
        return "feature/feature-per-publication";
    }

    @RequestMapping("/{pubID}/genotype-list")
    public String showGenotypeList(@PathVariable String pubID,
                                   @ModelAttribute("formBean") GeneBean geneBean,
                                   Model model,
                                   HttpServletResponse response) {
        logger.info("zdbID: " + pubID);

        Publication publication = getPublication(pubID);

        if (publication == null) {

            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        List<Genotype> genotypeList = publicationRepository.getGenotypesInPublication(publication.getZdbID());

        model.addAttribute("genotypeList", genotypeList);
        model.addAttribute("publication", publication);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Genotypes"));
        return "feature/genotype-per-publication";
    }

    @RequestMapping("/{pubID}/fish-list")
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
        return "fish/fish-per-publication";
    }


    @RequestMapping("/{zdbID}/disease")
    public String disease(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);

        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("diseases", phenotypeRepository.getHumanDiseaseModels(publication.getZdbID()));
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, getTitle(publication, "Human Disease / Zebrafish Models"));
        return "publication/publication-disease";
    }

    @RequestMapping("/{zdbID}/genes")
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
        return "publication/publication-marker-list";
    }

    @RequestMapping("/{zdbID}/efgs")
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
        return "publication/publication-egf-list";
    }

    @RequestMapping("/{zdbID}/clones")
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
        return "publication/publication-clone-list";
    }

    @RequestMapping("/{zdbID}/strs")
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
        return "publication/publication-str-list";
    }

    @ResponseBody
    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    public List<MarkerReferenceBean> publicationLookup(@RequestParam("q") String query) {
        List<MarkerReferenceBean> results = new ArrayList<>();
        Publication publication = publicationRepository.getPublication(query);
        if (publication != null) {
            results.add(MarkerReferenceBean.convert(publication));
        }
        return results;
    }

    @RequestMapping("/journal/{zdbID}")
    public String viewJournal(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Journal journal = publicationRepository.getJournalByID(zdbID);
        //try zdb_replaced data if necessary
        if (journal == null) {
            String replacedZdbID = infrastructureRepository.getWithdrawnZdbID(zdbID);
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
        return "publication/journal-view";
    }


    @RequestMapping("/image-edit")
    public String getImageEdit(Model model, @RequestParam("zdbID") String zdbID) {

        Image image = publicationRepository.getImageById(zdbID);
        if (image == null) {
            return null;
        }

        model.addAttribute("image", image);

        Figure figure = image.getFigure();
        if (figure != null) {
            Clone probe = figureViewService.getProbeForFigure(figure);
            model.addAttribute("probe", probe);
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Image: " + figureViewService.getFullFigureLabel(image.getFigure()));
        }

        return "figure/image-edit";
    }


    @RequestMapping("/printable/{zdbID}")
    public String printable(@PathVariable String zdbID, Model model, HttpServletResponse response) {
        Publication publication = getPublication(zdbID);
        if (publication == null) {
            response.setStatus(HttpStatus.SC_NOT_FOUND);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication.getPrintable());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        model.addAttribute("formattedDate", sdf.format(new Date()));

        return "publication/printable";
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

    @RequestMapping("/{pubID}/directly-attributed")
    public String showDirectlyAttributed(@PathVariable String pubID,
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

        Long numberOfDirectlyAttributed = publicationRepository.getDirectlyAttributed(publication);
        model.addAttribute("totalRecords", numberOfDirectlyAttributed);
        List<String> directedAttributedIds = RepositoryFactory.getPublicationRepository().getDirectlyAttributedZdbids(pubID, new Pagination());
        model.addAttribute("directedAttributedData", directedAttributedIds);
        model.addAttribute("publication", publication);
        return "publication/publication-directly-attributed";
    }

    @RequestMapping("/{pubID}/all-figures")
    public String showAllFigures(@PathVariable String pubID,
                                 @RequestParam(value = "probeZdbID", required = false) String probeZdbID,
                                 @RequestParam(value = "showDataOnly", required = false) boolean showDataOnly,
                                 Model model) {
        Publication publication = publicationRepository.getPublication(pubID);

        if (publication == null) {
            model.addAttribute(LookupStrings.ZDB_ID, pubID);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("publication", publication);
        model.addAttribute("showElsevierMessage", figureViewService.showElsevierMessage(publication));
        model.addAttribute("hasAcknowledgment", figureViewService.hasAcknowledgment(publication));
        model.addAttribute("showMultipleMediumSizedImages", figureViewService.showMultipleMediumSizedImages(publication));
        // model.addAttribute("isZebrasharePub", figureViewService.isZebrasharePub(publication));
        //for direct submission pubs, publication.getFigures() won't be correct and we'll need to do a query...
        List<Figure> figures = new ArrayList<>();

        //also for direct submission pubs, we should see if we got a probe
        Clone probe = null;
        if (!StringUtils.isEmpty(probeZdbID)) {
            probe = RepositoryFactory.getMarkerRepository().getCloneById(probeZdbID);
        }
        model.addAttribute("probe", probe);
        if (probe != null) {
            List<OrganizationLink> suppliers = RepositoryFactory.getProfileRepository().getSupplierLinksForZdbId(probe.getZdbID());
            model.addAttribute("probeSuppliers", suppliers);
        }
        if (figureViewService.isZebrasharePub(publication)) {
            figures.addAll(publication.getFigures());
        } else {
            if (publication.isUnpublished()) {
                if (!StringUtils.isEmpty(probeZdbID)) {
                    figures.addAll(getFigureRepository().getFiguresForDirectSubmissionPublication(publication, probe));
                } else {
                    figures.addAll(publication.getFigures());
                }
            } else {
                figures.addAll(publication.getFigures());
            }

        }


        figures.sort(ComparatorCreator.orderBy("orderingLabel", "zdbID"));

        model.addAttribute("submitters", getFigureRepository().getSubmitters(publication, probe));
        model.addAttribute("showThisseInSituLink", figureViewService.showThisseInSituLink(publication));
        model.addAttribute("showErrataAndNotes", figureViewService.showErrataAndNotes(publication));

        Map<Figure, FigureExpressionSummary> expressionSummaryMap = new HashMap<>();
        Map<Figure, FigurePhenotypeSummary> phenotypeSummaryMap = new HashMap<>();
        figures.forEach(figure -> {
            FigureExpressionSummary figureExpressionSummary = figureViewService.getFigureExpressionSummary(figure);
            FigurePhenotypeSummary figurePhenotypeSummary = figureViewService.getFigurePhenotypeSummary(figure);
            if (figureExpressionSummary.isNotEmpty())
                expressionSummaryMap.put(figure, figureExpressionSummary);
            if (figurePhenotypeSummary.isNotEmpty())
                phenotypeSummaryMap.put(figure, figurePhenotypeSummary);
        });

        List<Figure> figuresWithDataOnly = figures.stream()
            .filter(figure -> (expressionSummaryMap.get(figure) != null || phenotypeSummaryMap.get(figure) != null))
            .collect(Collectors.toList());
        if (!showDataOnly) {
            model.addAttribute("figures", figures);
            model.addAttribute("figureCaptions", figures.stream().map(Figure::getLabel).collect(toList()));
        }else {
            model.addAttribute("figures", figuresWithDataOnly);
            model.addAttribute("figureCaptions", figuresWithDataOnly.stream().map(Figure::getLabel).collect(toList()));
        }
        model.addAttribute("showDataOnly", showDataOnly);
        model.addAttribute("allFiguresCssClass", !showDataOnly ? "active" : "");
        model.addAttribute("dataFiguresCssClass", showDataOnly ? "active" : "");
        model.addAttribute("expressionSummaryMap", expressionSummaryMap);
        model.addAttribute("phenotypeSummaryMap", phenotypeSummaryMap);

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "All Figures, " + publication.getShortAuthorList());

        return "publication/publication-figures";
    }
}







