package org.zfin.antibody.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * Create a hyperlink to the antibody detail page.
 */
public class AntibodyPresentation extends EntityPresentation {

    private static final String uri = "antibody/detail?antibody.zdbID=";

    /**
     * Generates a link to the antibody detail page.
     *
     * @param antibody antibody
     * @return html for marker link
     */
    public static String getLink(Antibody antibody) {
        return getTomcatLink(uri, antibody.getZdbID(), antibody.getName());
    }

    public static String getName(Antibody antibody) {
        return getSpanTag("none", antibody.getName(), antibody.getName());
    }

}
