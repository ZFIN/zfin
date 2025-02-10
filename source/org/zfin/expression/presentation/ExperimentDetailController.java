package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.zfin.expression.Experiment;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

/**
 * Show pages and popups for Experiments, primarily listing conditions with notes
 */

@Controller
@RequestMapping("/experiment")
public class ExperimentDetailController {

    @RequestMapping("/{id}")
    protected String getExperimentPage(@PathVariable String id, Model model) {
        Experiment experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(id);

        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return LookupStrings.RECORD_NOT_FOUND_PAGE;
        }

        model.addAttribute("experiment", experiment);

        return "expression/experiment";
    }


    @RequestMapping("/popup/{id}")
    protected String getExperimentPopup(@PathVariable String id, Model model) {
        Experiment experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(id);

        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return LookupStrings.RECORD_NOT_FOUND_POPUP;
        }

        model.addAttribute("experiment", experiment);

        return "expression/experiment-popup";
    }

}


