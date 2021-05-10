package org.zfin.framework.presentation;

import org.zfin.expression.Figure;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class FigureData {

    private Set<Publication> publications;
    private Set<Figure> figures = new HashSet<Figure>();


    public Set<Publication> getPublications() {
        if (publications == null) {
            publications = new HashSet<Publication>(figures.size());
            for (Figure figure : figures)
                publications.add(figure.getPublication());
        }
        return publications;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void addFigure(Figure figure) {
        figures.add(figure);
    }

    public boolean isSingleFigure() {
        return figures.size() == 1;
    }

    public Figure getFigure() {
        return figures.iterator().next();
    }

    public Publication getPublication() {
        return getFigure().getPublication();
    }

    public int getNumberOfPublication() {
        return getPublications().size();
    }
}
