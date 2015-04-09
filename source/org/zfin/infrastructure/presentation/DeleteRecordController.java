package org.zfin.infrastructure.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.delete.DeleteEntityRule;
import org.zfin.infrastructure.delete.DeleteFeatureRule;
import org.zfin.infrastructure.delete.DeleteValidationReport;
import org.zfin.marker.service.DeleteService;

import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;

/**
 * Attempts to delete a marker and lands on a splash page to indicate success/failure.
 * <p/>
 */
@Controller
@RequestMapping("/infrastructure")
public class DeleteRecordController {

    private Logger logger = Logger.getLogger(DeleteRecordController.class);

    @Autowired
    DeleteService deleteService;

    @RequestMapping(value = "/deleteRecord/{zdbIDToDelete}")
    public String validateDelete(Model model
            , @PathVariable("zdbIDToDelete") String zdbIDToDelete
            , @ModelAttribute("formBean") DeleteRecordBean formBean
    ) throws Exception {

        formBean.setZdbIDToDelete(zdbIDToDelete);
        DeleteEntityRule rule = deleteService.getDeleteRule(zdbIDToDelete);
        if (rule != null) {
            List<DeleteValidationReport> validationReportList = rule.validate();
            model.addAttribute("validationReportList", validationReportList);
            model.addAttribute("entity", rule.getEntity());
        }
        formBean.setRecordToDeleteViewString(zdbIDToDelete);
        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "delete " + zdbIDToDelete);
        return "infrastructure/delete-record.page";
    }

    @RequestMapping(value = "/record-deleted")
    public String doDelete(Model model
            , @RequestParam(value = "zdbIDToDelete", required = true) String zdbID
            , @RequestParam(value = "removeFromTracking", required = false) String deleteFeatureTracking
            , @ModelAttribute("formBean") DeleteRecordBean formBean
    ) throws Exception {

        formBean.setRecordToDeleteViewString(zdbID);

        try {
            HibernateUtil.createTransaction();
            DeleteEntityRule rule = deleteService.getDeleteRule(zdbID);
            rule.prepareDelete();
            rule.logDeleteOperation();
            formBean.setRecordToDeleteViewString(rule.getEntity().getEntityName());
            formBean.setPublicationCurated(rule.getPublication());
            getInfrastructureRepository().deleteActiveEntity(zdbID);

            HibernateUtil.currentSession().flush();

            // if removing from feature tracking is needed
            if (rule instanceof DeleteFeatureRule) {
                ((DeleteFeatureRule) rule).clearTrackingRecords(zdbID, deleteFeatureTracking);
                formBean.setRemovedFromTracking(false);
            }


            HibernateUtil.flushAndCommitCurrentSession();

        } catch (Exception e) {
            logger.error("Can not delete " + formBean, e);
            HibernateUtil.rollbackTransaction();
            formBean.addError("Can not delete " + formBean + "<br>" + e.getMessage());
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "record deleted");
        return "infrastructure/record-deleted.page";
    }

}
