package org.zfin.expression.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.antibody.Antibody;
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

/**
 * TODO: add comments
 */
@Setter
@Getter
public class ProteinExpressionDisplay implements Comparable<ProteinExpressionDisplay> {
    @JsonView(View.API.class)
    private Antibody antibody;
    @JsonView(View.API.class)
    private Marker antiGene;    // nullable
    @JsonView(View.API.class)
    private Set<Publication> publications;
    private Set<Figure> figures;
    @JsonView(View.API.class)
    private List<ExpressionResult2> expressionResults;
    @JsonView(View.API.class)
    private Experiment experiment;
    @JsonView(View.API.class)
    private Set<GenericTerm> expressionTerms;
    private SortedMap<Publication, SortedSet<Figure>> figuresPerPub;

    public ProteinExpressionDisplay(Antibody antibody) {
        this.antibody = antibody;
    }

    public int compareTo(ProteinExpressionDisplay anotherProteinExpressionDisplay) {
        if (antiGene == null) {
            return -1;
        } else if (antiGene.compareTo(anotherProteinExpressionDisplay.getAntiGene()) == 0)
            return experiment.compareTo(anotherProteinExpressionDisplay.getExperiment());
        return antiGene.compareTo(anotherProteinExpressionDisplay.getAntiGene());
    }

    @JsonView(View.API.class)
    public int getNumberOfFigures() {
        if (figures == null) {
            return 0;
        } else {
            return figures.size();
        }
    }

    @JsonView(View.API.class)
    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            return null;

        Figure singleFigure = null;
        for (Figure fig : figures) {
            singleFigure = fig;
            break;
        }
        return singleFigure;
    }

    @JsonView(View.API.class)
    public Publication getPublication() {
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

    @JsonView(View.API.class)
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

    @JsonView(View.API.class)
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

}
