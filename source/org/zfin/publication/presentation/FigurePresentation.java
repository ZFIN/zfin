package org.zfin.publication.presentation;

import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityPresentation;

/**
 * To get/create output from a Figure object
 */
public class FigurePresentation extends EntityPresentation {
    private static final String uri = "?MIval=aa-fxfigureview.apg&OID=";

    /**
     * Generates a Figure link using the label.
     *
     * @return html for Figure link
     * @param figure figure
     */
    public static String getLink(Figure figure) {
        return getWebdriverLink(uri, figure.getZdbID(), figure.getLabel().replaceAll(" ", "&nbsp;"));
    }

    public static String getUrl(Figure figure) {
        return getWebdriverUrl(uri, figure.getZdbID());
    }

    public static String getLinkStartTag(Figure figure) {
        return getWebdriverStartTag(uri, figure.getZdbID());
    }


}
