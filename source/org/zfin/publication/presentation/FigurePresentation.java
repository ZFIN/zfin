package org.zfin.publication.presentation;

import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.framework.presentation.EntityPresentation;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;

/**
 * To get/create output from a Figure object
 */
public class FigurePresentation extends EntityPresentation {

    /**
     * Generates a Figure link using the label.
     *
     * @param figure figure
     * @return html for Figure link
     */
    public static String getLink(Figure figure) {
        return getViewLink(figure.getZdbID(), figure.getLabel().replaceAll(" ", "&nbsp;"), null, "figure-link");
    }

    public static String getLink(String zdbID, String linkText) {
        Figure figure = new FigureFigure();
        figure.setZdbID(zdbID);
        figure.setLabel(linkText);
        return getLink(figure);
    }

    /**
     * Generates a Figure link without the label but '1 Figure'
     *
     * @param figure figure
     * @return html for Figure link
     */
    public static String getSimpleLink(Figure figure) {
        return getViewLink(figure.getZdbID(), "1 Figure", null, "figure-link simple-figure-link");
    }

    public static String getUrl(Figure figure) {
        return getJumpToLink(figure.getZdbID());
    }

    public static String getLinkStartTag(Figure figure) {
        return getViewStartTag(figure.getZdbID());
    }


    public static String getLinkByZfinEntity(ZfinEntity entity) {
        Figure figure = new FigureFigure();
        figure.setZdbID(entity.getID());
        figure.setLabel(entity.getName());
        return getSimpleLink(figure);
    }
}
