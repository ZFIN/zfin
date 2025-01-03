package org.zfin.expression.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult2;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

@Setter
@Getter
public class ExpressionDisplay implements Comparable<ExpressionDisplay> {
    @JsonView(View.API.class)
    private Marker expressedGene;
    @JsonView(View.API.class)
    private Set<Publication> publications;
    @JsonView(View.API.class)
    private Set<Figure> figures;
    private List<ExpressionResult2> expressionResults;
    @JsonView(View.API.class)
    private Experiment experiment;
    @JsonView(View.API.class)
    private Set<GenericTerm> expressionTerms;
    private SortedMap<Publication, SortedSet<Figure>> figuresPerPub;

    public ExpressionDisplay(Marker expressedGene) {
        this.expressedGene = expressedGene;
    }

    public int compareTo(ExpressionDisplay anotherExpressionDisplay) {
        if (expressedGene == null) {
            return -1;
        } else if (expressedGene.compareTo(anotherExpressionDisplay.getExpressedGene()) == 0)
            return experiment.compareTo(anotherExpressionDisplay.getExperiment());
        return expressedGene.compareTo(anotherExpressionDisplay.getExpressedGene());
    }

    @JsonView(View.API.class)
    public int getNumberOfFigures() {
        if (figures == null) {
            return 0;
        } else {
            return figures.size();
        }
    }

    public Figure getSingleFigure() {
        if (figures == null || figures.size() != 1)
            return null;

        Figure singleFigure = null;
        for (Figure fig : figures) {
            singleFigure = fig;
            break;
        }
        return singleFigure;
    }

    public Publication getSinglePublication() {
        if (publications == null || publications.size() != 1)
            return null;

        Publication singlePub = new Publication();
        for (Publication pub : publications) {
            singlePub = pub;
        }
        return singlePub;
    }

    @JsonView(View.API.class)
    public int getNumberOfPublications() {
        if (publications == null) {
            return 0;
        } else {
            return publications.size();
        }
    }

    public Marker getExpressedGene() {
        return expressedGene;
    }

    public void setExpressedGene(Marker expressedGene) {
        this.expressedGene = expressedGene;
    }

    public boolean isImgInFigure() {
        if (noFigureOrFigureWithNoLabel()) {
            return false;
        }
        boolean thereIsImg = false;
        for (Figure fig : figures) {
            if (!fig.isImgless()) {
                thereIsImg = true;
                break;
            }
        }
        return thereIsImg;
    }

    public boolean noFigureOrFigureWithNoLabel() {
        if (figures == null || figures.isEmpty()) {
            return true;
        }
        boolean noFigureLabel = false;
        for (Figure fig : figures) {
            if (fig.getLabel() == null) {
                noFigureLabel = true;
                break;
            }
        }
        return noFigureLabel;
    }

    @JsonView(View.API.class)
    public Publication getFirstPublication() {
        return publications.iterator().next();
    }

    @JsonView(View.API.class)
    public Figure getFirstFigure() {
        return figures.iterator().next();
    }

}
