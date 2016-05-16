package org.zfin.infrastructure.presentation;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.zfin.construct.ConstructComponent;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.infrastructure.ControlledVocab;
import org.zfin.infrastructure.delete.DeleteEntityRule;
import org.zfin.infrastructure.delete.DeleteFeatureRule;
import org.zfin.infrastructure.delete.DeleteValidationReport;
import org.zfin.marker.service.DeleteService;

import javax.validation.Valid;
import java.util.List;

import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getConstructRepository;

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

    @RequestMapping(value = "/controlled-vocabulary-delete")
    public String doDelete(Model model
            , @RequestParam(value = "zdbIDToDelete", required = true) String zdbID
    ) throws Exception {

        List<ConstructComponent> constructComponents = getConstructRepository().getConstructComponentsByComponentID(zdbID);
        if (constructComponents != null && constructComponents.size() > 0) {
            model.addAttribute(LookupStrings.DYNAMIC_TITLE, "record could not be deleted");
            model.addAttribute("constructComponents", constructComponents);
            return "infrastructure/controlled-vocabulary-added.page";
        }

        try {
            HibernateUtil.createTransaction();
            getInfrastructureRepository().insertUpdatesTable(zdbID, "Controlled Vocabulary", zdbID, "", " Controlled Vocabulary deleted through UI");
            getInfrastructureRepository().deleteActiveEntity(zdbID);
            HibernateUtil.flushAndCommitCurrentSession();
        } catch (Exception e) {
            logger.error("Can not delete " + zdbID, e);
            HibernateUtil.rollbackTransaction();
        }

        model.addAttribute(LookupStrings.DYNAMIC_TITLE, "Controlled Vocabulary record deleted");

        return "infrastructure/controlled-vocabulary-add.page";
    }
}
