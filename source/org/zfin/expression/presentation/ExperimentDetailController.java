package org.zfin.expression.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.LookupStrings;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Show pages and popups for Experiments, primarily listing conditions with notes
 */

@Controller
@RequestMapping("/expression")
public class ExperimentDetailController {

    @RequestMapping("/experiment")
    protected String getExperimentPage(@RequestParam String id, Model model) {
        Experiment experiment = RepositoryFactory.getExpressionRepository().getExperimentByID(id);

        if (experiment == null) {
            model.addAttribute(LookupStrings.ZDB_ID, id);
            return LookupStrings.RECORD_NOT_FOUND_PAGE ;
        }

        model.addAttribute("experiment", experiment);

        Set<ExperimentCondition> conditions = getSortedConditions(experiment);
        model.addAttribute("sequenceTargetingReagentConditions", getSTRconditions(conditions));
        model.addAttribute("nonSequenceTargetingReagentConditions", getNonSTRconditions(conditions));

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
        model.addAttribute("sequenceTargetingReagentConditions", getSTRconditions(conditions));
        model.addAttribute("nonSequenceTargetingReagentConditions", getNonSTRconditions(conditions));

        return "expression/experiment-popup.popup";
    }

    protected Set<ExperimentCondition> getSortedConditions(Experiment experiment) {
        TreeSet<ExperimentCondition> conditions = new TreeSet<>();
        conditions.addAll(experiment.getExperimentConditions());
        return conditions;
    }

    protected List<ExperimentCondition> getSTRconditions(Set<ExperimentCondition> conditions) {
        List<ExperimentCondition> moConditions = new ArrayList<>();
        for (ExperimentCondition condition : conditions) {
            if (condition.getSequenceTargetingReagent() != null)
                moConditions.add(condition);
        }
        return moConditions;
    }

    protected List<ExperimentCondition> getNonSTRconditions(Set<ExperimentCondition> conditions) {
        List<ExperimentCondition> nonMoConditions = new ArrayList<>();
        for (ExperimentCondition condition : conditions) {
            if (condition.getSequenceTargetingReagent() == null) {
                nonMoConditions.add(condition);
            }
        }
        return nonMoConditions;
    }

}


