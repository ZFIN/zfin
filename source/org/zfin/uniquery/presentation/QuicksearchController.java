package org.zfin.uniquery.presentation;

import org.apache.lucene.search.BooleanQuery;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniquery.search.SearchResults;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for quicksearch
 */
public class QuicksearchController extends AbstractCommandController {


    public QuicksearchController() {
        setCommandClass(SearchBean.class);
        BooleanQuery.setMaxClauseCount(50000);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        SearchBean searchBean = (SearchBean) command;
        String queryTerm = searchBean.getQueryTerm();

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        ReplacementZdbID replacementZdbID = infrastructureRepository.getReplacementZdbId(queryTerm);
        searchBean.setReplacementZdbID(replacementZdbID);

        int numberOfRecords = 0;
        // if replaced ID found do not do a search
        if (queryTerm.length() > 0 && replacementZdbID == null) {
            SearchResults expressionResults = searchBean.doCategorySearch();
            searchBean.setSearchResult(expressionResults);
            numberOfRecords = expressionResults.getTotalHits();
        }
        searchBean.setTotalRecords(numberOfRecords);
        searchBean.setQueryString(request.getQueryString());
        searchBean.setRequestUrl(request.getRequestURL());

        return new ModelAndView("quick-search.page", LookupStrings.FORM_BEAN, searchBean);
    }
}
