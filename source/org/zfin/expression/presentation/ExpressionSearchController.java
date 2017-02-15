package org.zfin.expression.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/expression")
public class ExpressionSearchController {

    private static Logger logger = Logger.getLogger(ExpressionSearchController.class);

    private static final String BASE_URL = "/action/expression/results";

    @Autowired
    MarkerRepository markerRepository;

    @RequestMapping("/search")
    public String search(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria) {
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Expression Search");
        return "expression/search.page";
    }

    @RequestMapping("/results")
    public String results(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria, HttpServletRequest request) {

        if (criteria.getRows() == null) {
            criteria.setRows(20);
        }
        if (criteria.getPage() == null) {
            criteria.setPage(1);
        }

        List<ImageResult> images = ExpressionSearchService.getImageResults(criteria);
        criteria.setImageResults(images);

        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            criteria.setGene(markerRepository.getGeneByID(criteria.getGeneZdbID()));
            populateFigureResults(criteria);
        } else {
            //assuming we weren't doing a specific gene, build a gene results
            List<GeneResult> geneResults = ExpressionSearchService.getGeneResults(criteria);
            criteria.setGeneResults(geneResults);

            //if it happens that it's only one gene, do figure results after all...
            if (criteria.getGeneResults() != null && criteria.getGeneResults().size() == 1) {
                criteria.setGene(criteria.getGeneResults().get(0).getGene());
                criteria.setGeneZdbID(criteria.getGene().getZdbID());
                populateFigureResults(criteria);
            }
        }

        if (criteria.getNumFound() > 0) {
            model.addAttribute("paginationBean", generatePaginationBean(criteria, request.getQueryString()));
            criteria.setLinkWithImagesOnly(generateImagesOnlyUrl(request.getQueryString()));
        }

        model.addAttribute("criteria", criteria);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Expression Search Results");
        return "expression/results.page";
    }


    private PaginationBean generatePaginationBean(ExpressionSearchCriteria criteria, String queryString) {
        PaginationBean paginationBean = new PaginationBean();
        URLCreator paginationUrlCreator = new URLCreator(BASE_URL + "?" + queryString);
        paginationUrlCreator.removeNameValuePair("page");
        paginationBean.setActionUrl(paginationUrlCreator.getFullURLPlusSeparator());

        if (criteria.getPage() == null) {
            criteria.setPage(1);
        }
        if (criteria.getRows() == null) {
            criteria.setRows(20);
        }

        paginationBean.setPage(criteria.getPage().toString());
        paginationBean.setTotalRecords((int) criteria.getNumFound());
        paginationBean.setQueryString(paginationUrlCreator.getURL());
        paginationBean.setMaxDisplayRecords(criteria.getRows());

        return paginationBean;
    }

    private String generateImagesOnlyUrl(String queryString) {
        URLCreator urlCreator = new URLCreator(BASE_URL + "?" + queryString);
        urlCreator.removeNameValuePair("page");
        urlCreator.removeNameValuePair("onlyFiguresWithImages");
        urlCreator.addNamevaluePair("onlyFiguresWithImages", "true");
        return urlCreator.getURL();
    }

    private void populateFigureResults(ExpressionSearchCriteria criteria) {
        List<FigureResult> figureResults = ExpressionSearchService.getFigureResults(criteria);
        criteria.setFigureResults(figureResults);
    }
}
