package org.zfin.expression.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/expression")
public class ExpressionSearchController {

    private static Logger logger = Logger.getLogger(ExpressionSearchController.class);

    private static final String BASE_URL = "/action/expression/results?";
    public static final int DEFAULT_PAGE_SIZE = 25;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private InfrastructureRepository infrastructureRepository;

    @Autowired
    private ExpressionSearchService expressionSearchService;

    @ModelAttribute("assays")
    public List<ExpressionAssay> populateAssayOptions() {
        return infrastructureRepository.getAllAssays();
    }

    @ModelAttribute("journalTypeOptions")
    public ExpressionSearchCriteria.JournalTypeOption[] populateJournalTypeOptions() {
        return ExpressionSearchCriteria.JournalTypeOption.values();
    }

    @ModelAttribute("criteria")
    public ExpressionSearchCriteria getDefaultForm() {
        ExpressionSearchCriteria criteria = new ExpressionSearchCriteria();
        SortedMap<String, String> stages = expressionSearchService.getStageOptions();
        criteria.setStages(stages);
        criteria.setStartStageId(stages.firstKey());
        criteria.setEndStageId(stages.lastKey());
        criteria.setJournalType(ExpressionSearchCriteria.JournalTypeOption.ALL);
        criteria.setIncludeSubstructures(true);
        criteria.setRows(DEFAULT_PAGE_SIZE);
        criteria.setPage(1);
        return criteria;
    }

    @RequestMapping("/search")
    public String search(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Expression Search");
        return "expression/search.page";
    }


    @RequestMapping("/results")
    public String results(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria, HttpServletRequest request) {
        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            criteria.setGene(markerRepository.getMarkerByID(criteria.getGeneZdbID()));
            populateFigureResults(criteria);
        } else {
            //assuming we weren't doing a specific gene, build a gene results
            List<GeneResult> geneResults = expressionSearchService.getGeneResults(criteria);
            criteria.setGeneResults(geneResults);

            //if it happens that it's only one gene, do figure results after all...
            if (criteria.getGeneResults() != null && criteria.getGeneResults().size() == 1) {
                criteria.setGene(criteria.getGeneResults().get(0).getGene());
                criteria.setGeneZdbID(criteria.getGene().getZdbID());
                populateFigureResults(criteria);
            }
        }

        List<ImageResult> images = expressionSearchService.getImageResults(criteria);
        criteria.setImageResults(images);

        if (criteria.getNumFound() > 0) {
            model.addAttribute("paginationBean", generatePaginationBean(criteria, request.getQueryString()));
            criteria.setLinkWithImagesOnly(generateImagesOnlyUrl(request.getQueryString()));
        }

        model.addAttribute("criteria", criteria);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Expression Search Results");
        return "expression/results.page";
    }

    @RequestMapping("/xpatselect")
    public String resultsFromOldParams(
            @RequestParam(value = "gene_name", required = false) String geneField,
            @RequestParam(value = "xpatsel_geneZdbId", required = false) String geneZdbId,
            @RequestParam(value = "stage_start", required = false) Float startHours,
            @RequestParam(value = "stage_end", required = false) Float endHours,
            @RequestParam(value = "xpatsel_processed_selected_structures", required = false) List<String> termZdbIDs
    ) {

        ExpressionSearchService.LinkBuilder builder = new ExpressionSearchService.LinkBuilder();

        if (startHours != null) {
            DevelopmentStage start = RepositoryFactory.getAnatomyRepository().getStageByStartHours(startHours);
            if (start != null) { builder.startStage(start.getOboID()); }
        }

        if (endHours != null) {
            DevelopmentStage end = RepositoryFactory.getAnatomyRepository().getStageByEndHours(endHours);
            if (end != null) { builder.endStage(end.getOboID()); }
        }

        if (CollectionUtils.isNotEmpty(termZdbIDs)) {
            List<GenericTerm> terms = termZdbIDs.stream().map(RepositoryFactory.getOntologyRepository()::getTermByZdbID).collect(Collectors.toList());
            terms.forEach(term -> builder.anatomyTerm(term));
        }

        String link = builder
                .geneField(geneField)
                .geneZdbID(geneZdbId)
                .build();
        return "redirect:" + link;
    }



    private PaginationBean generatePaginationBean(ExpressionSearchCriteria criteria, String queryString) {
        PaginationBean paginationBean = new PaginationBean();

        if (queryString == null) { queryString = ""; }

        URLCreator paginationUrlCreator = new URLCreator(BASE_URL + queryString);
        paginationUrlCreator.removeNameValuePair("page");
        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            paginationUrlCreator.replaceNameValuePair("geneZdbID", criteria.getGeneZdbID());
        }
        paginationBean.setActionUrl(paginationUrlCreator.getFullURLPlusSeparator());

        if (criteria.getPage() == null) {
            criteria.setPage(1);
        }
        if (criteria.getRows() == null) {
            criteria.setRows(DEFAULT_PAGE_SIZE);
        }

        paginationBean.setPage(criteria.getPage().toString());
        paginationBean.setTotalRecords((int) criteria.getNumFound());
        paginationBean.setQueryString(paginationUrlCreator.getURL());
        paginationBean.setMaxDisplayRecords(criteria.getRows());

        return paginationBean;
    }

    private String generateImagesOnlyUrl(String queryString) {
        URLCreator urlCreator = new URLCreator(BASE_URL + queryString);
        urlCreator.removeNameValuePair("page");
        urlCreator.replaceNameValuePair("onlyFiguresWithImages", "true");
        return urlCreator.getURL();
    }

    private void populateFigureResults(ExpressionSearchCriteria criteria) {
        List<FigureResult> figureResults = expressionSearchService.getFigureResults(criteria);
        criteria.setFigureResults(figureResults);
    }
}
