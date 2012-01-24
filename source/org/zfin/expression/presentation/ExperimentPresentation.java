package org.zfin.expression.presentation;


import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;

import java.util.TreeSet;

public class ExperimentPresentation extends EntityPresentation {

    public static String getLink(Experiment experiment, boolean suppressPopupLink, boolean suppressMoDetails) {
        if (experiment == null)
            return null;
        if (experiment.isStandard())
            return "standard or control";
        if (experiment.isChemical())
            return "chemical";

        TreeSet<ExperimentCondition> conditions = new TreeSet<ExperimentCondition>();
        conditions.addAll(experiment.getExperimentConditions());

        StringBuilder sb = new StringBuilder(50);
        int i = 0;
        boolean hasMoDefined = false;
        for (ExperimentCondition experimentCondition : conditions) {
            if (i > 0)
                sb.append(", ");
            if (experimentCondition.isMoCondition() && suppressMoDetails) {
                if (!hasMoDefined) {
                    sb.append("Morpholino");
                    hasMoDefined = true;
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


    //this could share code more elegantly with the getLink method above...
    public static String getName(Experiment experiment) {
        if (experiment == null)
            return null;
        if (experiment.isStandard())
            return "standard or control";
        if (experiment.isChemical())
            return "chemical";

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

}
