package org.zfin.anatomy.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Create a hyperlink to development stage pages.
 */
public class DevelopmentStagePresentation extends EntityPresentation {

    private static final String uri = "/zf_info/zfbook/stages/index.html#";

    /**
     * Generates a Go stage link using the go id
     *
     * @param stage       Go stage
     * @param longVersion boolean
     * @return html for marker link
     */
    public static String getLink(DevelopmentStage stage, boolean longVersion) {
        if (longVersion) {
            String stageName = StagePresentation.createDisplayEntry(stage);
            return getGeneralHyperLink(uri + stage.abbreviation(), stageName);
        } else
            return getGeneralHyperLink(uri + stage.abbreviation(), stage.getAbbreviation());
    }

    /**
     * Generate the name of the link
     *
     * @param stage DevelopmentStage
     * @return name of the hyerplink.
     */
    public static String getName(DevelopmentStage stage) {
        return getSpanTag("none", stage.getName(), stage.getName());
    }

}
