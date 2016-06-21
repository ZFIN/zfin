package org.zfin.expression.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;

import java.util.Set;

/**
 * definitions of hyperlinks and other output formats for ExperimentCondition.
 */
public class ExperimentConditionPresentation extends EntityPresentation {

    private static final String uri = "expression/experiment?id=";
    private static final String popupUri = "expression/experiment-popup?id=";


    /**
     * Generates a condition link, either a marker link for the morpholino or an
     * xpatexcdndisplay link using the condition as the name
     *
     * @param condition ExperimentCondition
     * @return html for marker link
     */
    public static String getLink(ExperimentCondition condition, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(getTomcatLink(uri, condition.getExperiment().getZdbID(), getName(condition)));

        if (!suppressPopupLink)
            sb.append(getPopupLink(condition));

        return sb.toString();
    }

    /**
     * Generates a condition link
     *
     * @param conditions set of ExperimentConditions
     * @return html for marker link
     */
    public static String getLink(Set<ExperimentCondition> conditions, Experiment experiment, boolean suppressPopupLink) {
        StringBuilder sb = new StringBuilder(50);
        if (CollectionUtils.isNotEmpty(conditions)) {
            for (ExperimentCondition condition : conditions) {
                sb.append(getName(condition));
                sb.append(", ");
            }
            sb = sb.deleteCharAt(sb.length() - 1);
            sb = sb.deleteCharAt(sb.length() - 1);
        }

        String linkText = sb.toString();
        if (StringUtils.isBlank(linkText)) {
            return "";
        }

        sb = new StringBuilder(getTomcatLink(uri, experiment.getZdbID(), linkText));

        if (!suppressPopupLink) {
            sb.append(getPopupLink(experiment));
        }

        return sb.toString();
    }

    /**
     * show a name for a condition, uses the marker if it's a morpholino, uses
     * the condition group if it's not.  Never shows the actual condition, just
     * the group.
     *
     * @param condition ExperimentCondition
     * @return name of condition
     */
    public static String getName(ExperimentCondition condition) {
        return condition.getDisplayName();
    }

    public static String getPopupLink(ExperimentCondition condition) {
        if (condition == null)
            return null;
        return getTomcatPopupLink(popupUri, condition.getExperiment().getZdbID(), "Further explanation of experimental conditions");
    }

    public static String getPopupLink(Experiment experiment) {
        if (experiment == null)
            return null;
        return getTomcatPopupLink(popupUri, experiment.getZdbID(), "Further explanation of experimental conditions");
    }

}
