package org.zfin.expression.presentation;

import org.zfin.expression.ExperimentCondition;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.marker.presentation.MarkerPresentation;

/**
 * definitions of hyperlinks and other output formats for ExperimentCondition.
 */
public class ExperimentConditionPresentation extends EntityPresentation {

    private static final String uri = "?MIval=aa-xpatexpcdndisplay.apg&cdp_exp_zdb_id=";

    /**
     * Generates a Genotype link using the Abbreviation
     *
     * @param condition ExperimentCondition
     * @return html for marker link
     */
    public static String getLink(ExperimentCondition condition) {
        if (condition.getMorpholino() != null) {
            String name = condition.getMorpholino().getName();
            if (condition.getValue() != null)
                name += " (" + condition.getValue() + " " + condition.getUnit().getName() + ")";
            return getWebdriverLink(MarkerPresentation.marker_uri, condition.getMorpholino().getZdbID(), name);
        } else
            return "no morpholino";
    }

}
