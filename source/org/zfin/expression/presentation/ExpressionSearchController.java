package org.zfin.expression.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.service.ExpressionSearchService;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/expression")
public class ExpressionSearchController {

    private static Logger logger = Logger.getLogger(ExpressionSearchController.class);


    @RequestMapping("/search")
    public String search(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria) {
        return "expression/search.page";
    }

    @RequestMapping("/results")
    public String results(Model model, @ModelAttribute("criteria") ExpressionSearchCriteria criteria, HttpServletRequest request) {

        if (criteria.getRows() == null) { criteria.setRows(20); }
        if (criteria.getPage() == null) { criteria.setPage(1); }

        List<ImageResult> images = ExpressionSearchService.getImageResults(criteria);
        criteria.setImageResults(images);

        if (StringUtils.isNotEmpty(criteria.getGeneZdbID())) {
            List<FigureResult> figureResults = ExpressionSearchService.getFigureResults(criteria);
            criteria.setFigureResults(figureResults);

            if (criteria.getNumFound() != null && criteria.getNumFound() > 0) {
                model.addAttribute("paginationBean", generatePaginationBean(criteria, request.getQueryString()));
            }

            model.addAttribute("criteria",criteria);
            return "expression/results.page";
        }

        //assuming we weren't doing a specific gene, build a gene results
        List<GeneResult> geneResults = ExpressionSearchService.getGeneResults(criteria);
        criteria.setGeneResults(geneResults);

        //if it happens that it's only one gene, do figure results after all...
        if (criteria.getGeneResults() != null && criteria.getGeneResults().size() == 1) {
            List<FigureResult> figureResults = ExpressionSearchService.getFigureResults(criteria);
            criteria.setFigureResults(figureResults);
        }

        if (criteria.getNumFound() != null && criteria.getNumFound() > 0) {
            model.addAttribute("paginationBean", generatePaginationBean(criteria, request.getQueryString()));
        }

        model.addAttribute("criteria",criteria);
        return "expression/results.page";
    }


    private PaginationBean generatePaginationBean(ExpressionSearchCriteria criteria, String queryString) {
        PaginationBean paginationBean = new PaginationBean();
        URLCreator paginationUrlCreator = new URLCreator("/action/expression/results?" + queryString);
        paginationUrlCreator.removeNameValuePair("page");
        paginationBean.setActionUrl(paginationUrlCreator.getFullURLPlusSeparator());

        if (criteria.getPage() == null) { criteria.setPage(1); }
        if (criteria.getRows() == null) { criteria.setRows(20); }

        paginationBean.setPage(criteria.getPage().toString());
        paginationBean.setTotalRecords(criteria.getNumFound().intValue());
        paginationBean.setQueryString(paginationUrlCreator.getURL());
        paginationBean.setMaxDisplayRecords(criteria.getRows());

        return paginationBean;
    }
}
