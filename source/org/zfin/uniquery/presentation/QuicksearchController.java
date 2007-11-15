package org.zfin.uniquery.presentation;

import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.zfin.audit.presentation.AuditLogBean;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.AuditLogItem;
import org.zfin.repository.RepositoryFactory;
import org.zfin.uniquery.search.SearchBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for the Audit Log.
 */
public class QuicksearchController extends AbstractCommandController {

    public QuicksearchController() {
        setCommandClass(SearchBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        SearchBean form = (SearchBean) command;

        return new ModelAndView("quick-search-page", "searchBean", form);
    }
}
