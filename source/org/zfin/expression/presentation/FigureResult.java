package org.zfin.expression.presentation;

import org.zfin.expression.Figure;
import org.zfin.mutant.Fish;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;

import java.util.Collection;

public class FigureResult extends ExpressionSearchResult {

    private Publication publication;
    private Figure figure;
    private Collection<Fish> fish;
    private Collection<PostComposedEntity> anatomy;

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Collection<Fish> getFish() {
        return fish;
    }

    public void setFish(Collection<Fish> fish) {
        this.fish = fish;
    }

    public Collection<PostComposedEntity> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(Collection<PostComposedEntity> anatomy) {
        this.anatomy = anatomy;
    }
}
