package org.zfin.expression.presentation;

import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.presentation.MarkerPresentation;

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
        if (condition.getMorpholino() != null)
            return MarkerPresentation.getLink(condition.getMorpholino());
        else
            sb.append(getTomcatLink(uri,condition.getExperiment().getZdbID(),getName(condition)));

        if (!suppressPopupLink)
            sb.append(getPopupLink(condition));

        return sb.toString();
    }

    /**
     * show a name for a condition, uses the marker if it's a morpholino, uses
     * the condition group if it's not.  Never shows the actual condition, just
     * the group.
     * @param condition
     * @return
     */
    public static String getName(ExperimentCondition condition) {
        if (condition.getMorpholino() != null) {
            return MarkerPresentation.getName(condition.getMorpholino());
        } else
            return condition.getConditionDataType().getGroup() + ":" + condition.getConditionDataType().getName();
    }

    public static String getPopupLink(ExperimentCondition condition) {
        if (condition == null)
            return null;
        return getTomcatPopupLink(popupUri,condition.getExperiment().getZdbID(),"Further explanation of experimental conditions");
    }

}
