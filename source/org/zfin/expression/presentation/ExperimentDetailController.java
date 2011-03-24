package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

/**
 * Show pages and popups for Experiments, primarily listing conditions with notes
 */

@Controller
public class ExperimentDetailController {

    @RequestMapping("/experiment")
    protected String getExperimentPage(@RequestParam String id, Model model) {
        Experiment experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(id);

        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.page";
        }

        model.addAttribute("experiment", experiment);

        Set<ExperimentCondition> conditions = getSortedConditions(experiment);
        model.addAttribute("morpholinoConditions", getMorpholinoConditions(conditions));
        model.addAttribute("nonMorpholinoConditions", getNonMorpholinoConditions(conditions));

        return "expression/experiment.page";
    }


    @RequestMapping("/experiment-popup")
    protected String getExperimentPopup(@RequestParam String id, Model model) {
                Experiment experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(id);

        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return "record-not-found.popup";
        }


        model.addAttribute("experiment", experiment);

        Set<ExperimentCondition> conditions = getSortedConditions(experiment);
        model.addAttribute("morpholinoConditions", getMorpholinoConditions(conditions));
        model.addAttribute("nonMorpholinoConditions", getNonMorpholinoConditions(conditions));

        return "expression/experiment-popup.popup";
    }

    protected Set<ExperimentCondition> getSortedConditions(Experiment experiment) {
        TreeSet<ExperimentCondition> conditions = new TreeSet<ExperimentCondition>();
        conditions.addAll(experiment.getExperimentConditions());
        return conditions;
    }

    protected List<ExperimentCondition> getMorpholinoConditions(Set<ExperimentCondition> conditions) {
        List<ExperimentCondition> moConditions = new ArrayList<ExperimentCondition>();
        for (ExperimentCondition condition : conditions) {
            if (condition.getMorpholino() != null)
                moConditions.add(condition);
        }
        return moConditions;
    }

    protected List<ExperimentCondition> getNonMorpholinoConditions(Set<ExperimentCondition> conditions) {
        List<ExperimentCondition> nonMoConditions = new ArrayList<ExperimentCondition>();
        for (ExperimentCondition condition : conditions) {
            if (condition.getMorpholino() == null) {
                nonMoConditions.add(condition);
            }
        }
        return nonMoConditions;
    }

}


