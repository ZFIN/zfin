package org.zfin.uniquery.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.uniquery.search.SearchBean;
import org.zfin.uniquery.search.SearchResults;
import org.zfin.infrastructure.ReplacementZdbID;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for quicksearch
 */
public class QuicksearchController extends AbstractCommandController {

    public QuicksearchController() {
        setCommandClass(SearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        SearchBean searchBean = (SearchBean) command;
        String queryTerm = searchBean.getQueryTerm();

        InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();
        ReplacementZdbID replacementZdbID = infrastructureRepository.getReplacementZdbId(queryTerm);
        searchBean.setReplacementZdbID(replacementZdbID);

        SearchResults expressionResults = null;
        if (queryTerm.length() > 0 && replacementZdbID == null) {
            expressionResults = searchBean.doCategorySearch();
            searchBean.setSearchResult(expressionResults);
        }

        return new ModelAndView("quick-search.page", LookupStrings.FORM_BEAN, searchBean);
    }
}
