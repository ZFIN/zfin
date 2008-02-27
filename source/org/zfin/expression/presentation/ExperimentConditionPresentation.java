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
        if (condition.getMorpholino() != null)
            return getWebdriverLink(MarkerPresentation.uri, condition.getMorpholino().getZdbID(), condition.getMorpholino().getName());
        else
        return "no morpholino";
/*
            throw new RuntimeException("Not yet implemented. Please provide a logic to create the hyperlink for " +
                    "a non-morpholino");
*/
    }

}
