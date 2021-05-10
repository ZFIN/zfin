package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.ontology.GenericTerm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExpressionStageAnatomy {
    private DevelopmentStage stage;
    private List<GenericTerm> anatomyTerms;
    private HashSet<Figure> figures;

    public void addAnatomyTerm(GenericTerm term) {
        if (anatomyTerms == null)
            anatomyTerms = new ArrayList<GenericTerm>();
        //don't add duplicates, this is kind of an expensive way, but oh well.
        if (!anatomyTerms.contains(term))
            anatomyTerms.add(term);
    }


    public void addFigure(Figure fig) {
        if (figures == null) figures = new HashSet<Figure>();
        figures.add(fig);
    }

    public int getFigureCount() {
        return figures.size();
    }

    public HashSet<Figure> getFigures() {
        return figures;
    }

    public void setFigures(HashSet<Figure> figures) {
        this.figures = figures;
    }

    public DevelopmentStage getStage() {
        return stage;
    }

    public void setStage(DevelopmentStage stage) {
        this.stage = stage;
    }

    public List<GenericTerm> getAnatomyTerms() {
        return anatomyTerms;
    }

    public void setAnatomyTerms(List<GenericTerm> anatomyTerms) {
        this.anatomyTerms = anatomyTerms;
    }

    public String toString() {
        return stage.toString();
    }

}
