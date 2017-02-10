package org.zfin.expression.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.mutant.Fish;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.List;

public class FigureResult {

    private Publication publication;
    private Figure figure;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Fish fish;
    private List<Term> anatomy;

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

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public List<Term> getAnatomy() {
        return anatomy;
    }

    public void setAnatomy(List<Term> anatomy) {
        this.anatomy = anatomy;
    }
}
