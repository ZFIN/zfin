package org.zfin.expression;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExpressionStageAnatomy {
    private DevelopmentStage stage;
    private List<AnatomyItem> anatomyTerms;
    private HashSet<Figure> figures;

    public void addAnatomyTerm(AnatomyItem anat) {
        if (anatomyTerms == null)
            anatomyTerms = new ArrayList<AnatomyItem>();
        //don't add duplicates, this is kind of an expensive way, but oh well.
        if (anatomyTerms.contains(anat) == false)
            anatomyTerms.add(anat);
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

    public List<AnatomyItem> getAnatomyTerms() {
        return anatomyTerms;
    }

    public void setAnatomyTerms(List<AnatomyItem> anatomyTerms) {
        this.anatomyTerms = anatomyTerms;
    }


    public String toString() {
        return stage.toString();
    }

}
