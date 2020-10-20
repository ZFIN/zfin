package org.zfin.audit.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.audit.AuditLogItem;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

/**
 * Controller for the Audit Log.
 */
@Controller
public class DetailsController {


    @RequestMapping("/audit-logs/details")
    protected String geneFamilyHandler(@ModelAttribute("auditLogForm") AuditLogBean auditLogForm) throws Exception {

        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        List<AuditLogItem> items = alr.getAuditLogItems(auditLogForm.getZdbID());
        auditLogForm.setItems(items);

        return "auditLog/audit_log_details";
    }
}
