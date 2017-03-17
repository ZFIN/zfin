package org.zfin.expression.presentation;


import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class ExperimentPresentation extends EntityPresentation {

    // the following 2 variables are used for conditions
    private static final String experimentUri = "expression/experiment?id=";
    private static final String experimentPopupUri = "expression/experiment-popup?id=";

    public static String getLink(Experiment experiment, boolean suppressPopupLink) {
        if (experiment == null)
            return null;

        //if (experiment.isOnlyStandard())
        //    return "standard";
        if (experiment.isOnlyControl())
            return "control";
        //if (experiment.isStandard())
        //    return "standard or control";
        List<ExperimentCondition> conditions = new ArrayList<>();
        conditions.addAll(experiment.getExperimentConditions());
        Collections.sort(conditions);

        return ExperimentConditionPresentation.getLink(conditions, experiment, suppressPopupLink);
    }


    /**
     * @param experiment Experiment
     * @return This method is used to generate conditions in the summary section and the table section (fish column) in a figure view page.
     * <p/>
     * Compared to getLink(), getLinkWithChemical Details does not have .isChemical() condition checking, which allows a popup
     * to show up alongside chemical in a figure view table.
     * Also, standard or control does not show in a table, so if experiment.isStandard() is true, empty string is returned.
     */

    public static String getLinkWithChemicalDetails(Experiment experiment) {
        if (experiment == null)
            return null;
        if (experiment.isStandard())
            return "";

        StringBuilder sb = new StringBuilder(50);
        String experimentName = getNameWithChemicalDetails(experiment);

        if (StringUtils.isNotEmpty(experimentName)) {
            sb.append(getTomcatLink(experimentUri, experiment.getZdbID(), experimentName));
            sb.append(getTomcatPopupLink(experimentPopupUri, experiment.getZdbID(), "Further explanation of experimental conditions"));
        }

        return sb.toString();
    }


    public static String getName(Experiment experiment, boolean suppressChemicalDetails) {
        if (experiment == null)
            return null;
        if (suppressChemicalDetails) {
            if (experiment.isStandard())
                return "standard or control";
            if (experiment.isChemical())
                return "chemical";
        }

        TreeSet<ExperimentCondition> conditions = new TreeSet<>();
        conditions.addAll(experiment.getExperimentConditions());

        StringBuilder sb = new StringBuilder(50);
        int i = 0;
        for (ExperimentCondition experimentCondition : conditions) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ExperimentConditionPresentation.getName(experimentCondition));
            i++;

        }
        return sb.toString();
    }

    public static String getName(Experiment experiment) {
        return getName(experiment, true);
    }

    public static String getNameForFaceted(Experiment experiment) {
        if (experiment == null)
            return null;

        if (experiment.isOnlyStandard())
            return "standard";
        if (experiment.isOnlyControl())
            return "control";
        TreeSet<ExperimentCondition> conditions = new TreeSet<>();
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

    public static String getNameWithChemicalDetails(Experiment experiment) {
        return getName(experiment, false);
    }

}
