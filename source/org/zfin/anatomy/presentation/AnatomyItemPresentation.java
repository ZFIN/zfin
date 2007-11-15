package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Presentation Class to create output from a Run object.
 */
public class AnatomyItemPresentation extends EntityPresentation {

    private static final String uri = "anatomy/term-detail?anatomyItem.zdbID=";

    /**
     * Generates a Marker link using the Abbreviation
     *
     * @return html for marker link
     * @param anatomyItem Run
     */
    public static String getLink(AnatomyItem anatomyItem) {
        return getTomcatLink(uri, anatomyItem.getZdbID(), anatomyItem.getName());
    }

    public static String getName(AnatomyItem anatomyItem) {
        return getSpanTag("none", anatomyItem.getName(), anatomyItem.getName());
    }
}
