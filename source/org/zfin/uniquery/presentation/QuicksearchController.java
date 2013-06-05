package org.zfin.uniquery.presentation;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.BooleanQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniquery.SiteSearchIndexService;
import org.zfin.uniquery.SiteSearchService;
import org.zfin.uniquery.search.RelatedTermsService;
import org.zfin.util.ZfinStringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for site search
 */
@Controller
public class QuicksearchController {

    @Autowired
    private SiteSearchIndexService siteSearchIndexService;

    @Autowired
    private SiteSearchService siteSearchService;

    @Autowired
    private RelatedTermsService relatedTermsService;


    static {
        BooleanQuery.setMaxClauseCount(200000);
    }

    @ModelAttribute("formBean")
    private SearchBean getDefaultBean() {
        return new SearchBean();
    }

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public String getIndexSummary(Model model,
                                  @ModelAttribute("formBean") SearchBean searchBean) throws Exception {

        String query = searchBean.getQuery();
        SiteSearchHelper siteSearchHelper = new SiteSearchHelper(searchBean);
        siteSearchHelper.setRelatedTermsService(relatedTermsService);
        siteSearchHelper.setSiteSearchIndexService(siteSearchIndexService);

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        String queryTerm = SiteSearchHelper.getQueryTerm(query);
        ReplacementZdbID replacementZdbID = infrastructureRepository.getReplacementZdbId(queryTerm);
        searchBean.setReplacementZdbID(replacementZdbID);

        int numberOfRecords = 0;
        // if replaced ID found do not do a search
        if (StringUtils.isNotEmpty(queryTerm) && replacementZdbID == null) {
            siteSearchHelper.doCategorySearch();
            numberOfRecords = siteSearchHelper.getSearchResults().getTotalHits();
        }
        searchBean.setTotalRecords(numberOfRecords);
        searchBean.setQueryString(request.getQueryString());
        searchBean.setRequestUrl(request.getRequestURL());
        model.addAttribute(siteSearchHelper);

        return "site-search.page";
    }

}
