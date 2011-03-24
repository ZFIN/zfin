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
        return getGeneralHyperLink(uri + stage.abbreviation(), getName(stage, longVersion));
    }

    /**
     * Generate the name of the link
     *
     * @param stage DevelopmentStage
     * @param longVersion display the long version if true
     * @return name of the hyerplink.
     */
    public static String getName(DevelopmentStage stage, boolean longVersion) {
        String stageName;
        String longName = StagePresentation.createDisplayEntry(stage);
        if (longVersion)
            stageName = longName;
        else
            stageName = stage.getAbbreviation();

        return getSpanTag("stage", longName, stageName);
    }

}
