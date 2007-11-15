package org.zfin.audit.presentation;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Controller for the Audit Log.
 */
public class DetailsController extends AbstractCommandController {

    public DetailsController() {
        setCommandClass(AuditLogBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        AuditLogBean auditLogForm = (AuditLogBean) command;

        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        List<AuditLogItem> items = alr.getAuditLogItems(auditLogForm.getZdbID());
        auditLogForm.setItems(items);

        return new ModelAndView("audit-log-details", "auditLogForm", auditLogForm);
    }
}
