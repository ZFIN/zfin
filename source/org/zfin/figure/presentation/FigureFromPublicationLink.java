package org.zfin.figure.presentation;

import org.zfin.expression.Figure;
import org.zfin.framework.presentation.ProvidesLink;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.publication.presentation.PublicationPresentation;

public class FigureFromPublicationLink implements ProvidesLink {

    private Figure figure;

    public FigureFromPublicationLink() {}

    public FigureFromPublicationLink(Figure figure) {
        this.figure = figure;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    @Override
    public String getLink() {
        return FigurePresentation.getLink(figure) + " from " + PublicationPresentation.getLink(figure.getPublication());
    }

    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

}
