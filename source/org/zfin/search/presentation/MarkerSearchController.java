package org.zfin.search.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.search.service.MarkerSearchService;
import org.zfin.util.URLCreator;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by kschaper on 7/22/16.
 */
@Controller
@RequestMapping("/marker")
public class MarkerSearchController {

    @Autowired
    private MarkerSearchService markerSearchService;

    private static Logger logger = Logger.getLogger(MarkerSearchController.class);

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/search")
    public String search(Model model) {

        MarkerSearchCriteria criteria = new MarkerSearchCriteria();
        criteria.setPage(1);
        criteria.setRows(20);

        markerSearchService.injectFacets(criteria);

        model.addAttribute("criteria", criteria);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Marker Search");

        return "search/marker-search-results.page";
    }

    @RequestMapping(value = "/search-results")
    public String results(Model model, @ModelAttribute("criteria") MarkerSearchCriteria criteria, HttpServletRequest request) {

        criteria.setBaseUrl(getBaseUrl(criteria, request));
        markerSearchService.injectFacets(criteria);
        model.addAttribute("criteria", markerSearchService.injectResults(criteria));

        //redirect to the only result, if there's just one
        if (criteria.getNumFound() == 1) {
            return "redirect:/" + criteria.getResults().iterator().next().getMarker().getZdbID();
        }

        if (criteria.getNumFound() != null && criteria.getNumFound() > 0) {
            model.addAttribute("paginationBean", generatePaginationBean(criteria, request.getQueryString()));
        }

        criteria.setSearchHappened(true);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Marker Search Results");

        return "search/marker-search-results.page";
    }

    private PaginationBean generatePaginationBean(MarkerSearchCriteria criteria, String queryString) {
        PaginationBean paginationBean = new PaginationBean();
        URLCreator paginationUrlCreator = new URLCreator(criteria.getBaseUrl());
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

    public static String getBaseUrl(MarkerSearchCriteria criteria, HttpServletRequest request) {
        return request.getRequestURI() + "?" + request.getQueryString();
    }

}
