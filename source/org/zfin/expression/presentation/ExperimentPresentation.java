package org.zfin.expression.presentation;


import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;

import java.util.TreeSet;

public class ExperimentPresentation extends EntityPresentation {

    // the following 2 variables are used for conditions
    private static final String experimentUri = "expression/experiment?id=";
    private static final String experimentPopupUri = "expression/experiment-popup?id=";

    public static String getLink(Experiment experiment, boolean suppressPopupLink, boolean suppressMoDetails) {
        if (experiment == null)
            return null;

        if (experiment.isChemical())
            return "chemical";
        if (experiment.isOnlyStandard())
            return "standard";
        if (experiment.isOnlyControl())
            return "control";
        if (experiment.isStandard())
            return "standard or control";
        TreeSet<ExperimentCondition> conditions = new TreeSet<ExperimentCondition>();
        conditions.addAll(experiment.getExperimentConditions());

        StringBuilder sb = new StringBuilder(50);
        int i = 0;
        boolean sequenceTargetingReagentFound = false;
        for (ExperimentCondition experimentCondition : conditions) {
            if (i > 0)
                sb.append(", ");
            // Todo: may need to check STR from somewhere else?
            if (experimentCondition.isSequenceTargetingReagentCondition() && suppressMoDetails) {
                if (!sequenceTargetingReagentFound) {
                    sb.append("Sequence Targeting Reagent");
                    sequenceTargetingReagentFound = true;
                } else { // remove comma and white space.
                    if (i > 0)
                        sb.delete(sb.length() - 2, sb.length() - 1);
                }
            } else
                sb.append(ExperimentConditionPresentation.getLink(experimentCondition, suppressPopupLink));
            i++;
        }
        return sb.toString();
    }


    /**
     *
     * @param experiment
     * @return
     *
     * This method is used to generate conditions in the summary section and the table section (fish column) in a figure view page.
     *
     * Compared to getLink(), getLinkWithChemical Details does not have .isChemical() condition checking, which allows a popup
     * to show up alongside chemical in a figure view table.
     * Also, standard or control does not show in a table, so if experiment.isStandard() is true, empty string is returned.
     *
     */

    public static String getLinkWithChemicalDetails(Experiment experiment) {
        if (experiment == null)
            return null;
        if (experiment.isStandard())
            return "";

        StringBuilder sb = new StringBuilder(50);
        String experimentName = getNameWithChemicalDetails(experiment);

        if (StringUtils.isNotEmpty(experimentName)) {
            sb.append(getTomcatLink(experimentUri,experiment.getZdbID(),experimentName));
            sb.append(getTomcatPopupLink(experimentPopupUri,experiment.getZdbID(),"Further explanation of experimental conditions"));
        }

        return sb.toString();
    }


    //this could share code more elegantly with the getLink method above...
    public static String getName(Experiment experiment, boolean suppressChemicalDetails) {
        if (experiment == null)
            return null;
        if (suppressChemicalDetails) {
            if (experiment.isStandard())
                return "standard or control";
            if (experiment.isChemical())
                return "chemical";
        }

        TreeSet<ExperimentCondition> conditions = new TreeSet<ExperimentCondition>();
        conditions.addAll(experiment.getExperimentConditions());

        StringBuilder sb = new StringBuilder(50);
        int i = 0;
        for (ExperimentCondition experimentCondition : conditions) {
            // Todo: may need to check STR from somewhere else?
            if (!experimentCondition.isSequenceTargetingReagentCondition()) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(ExperimentConditionPresentation.getName(experimentCondition));
                i++;
            }

        }
        return sb.toString();
    }

    public static String getName(Experiment experiment) {
        return getName(experiment, true);
    }
    public static String getNameForFaceted(Experiment experiment, boolean suppressChemicalDetails) {
        if (experiment == null)
            return null;

        if (experiment.isOnlyStandard())
            return "standard";
        if (experiment.isOnlyControl())
            return "control";
        TreeSet<ExperimentCondition> conditions = new TreeSet<ExperimentCondition>();
        conditions.addAll(experiment.getExperimentConditions());

        StringBuilder sb = new StringBuilder(50);
        int i = 0;
        for (ExperimentCondition experimentCondition : conditions) {
            if (i > 0)
                sb.append(", ");
            sb.append(ExperimentConditionPresentation.getName(experimentCondition));
            i++;
        }
        return sb.toString();
    }

    //this is called by getLinkWithChemicalDetails. needs to be refactored so that it shares code with getName()
    public static String getNameWithChemicalDetails(Experiment experiment) {
        return getName(experiment, false);
    }

}
