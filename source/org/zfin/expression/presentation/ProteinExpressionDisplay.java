package org.zfin.expression.presentation;

import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 *  TODO: add comments
 */
public class ProteinExpressionDisplay implements Comparable<ProteinExpressionDisplay> {
    private Antibody antibody;
    private Marker antiGene;    // nullable
    private Set<Publication> publications;
    private Set<Figure> figures;
    private List<ExpressionResult> expressionResults;
    private Experiment experiment;
    private Set<GenericTerm> expressionTerms;
    private SortedMap<Publication, SortedSet<Figure>> figuresPerPub;

    public ProteinExpressionDisplay(Antibody antibody) {
        this.antibody = antibody;
    }

    public int compareTo(ProteinExpressionDisplay anotherProteinExpressionDisplay) {
        if (antiGene == null){
            return -1;
        }
        else if (antiGene.compareTo(anotherProteinExpressionDisplay.getAntiGene()) == 0)
            return experiment.compareTo(anotherProteinExpressionDisplay.getExperiment());
        return antiGene.compareTo(anotherProteinExpressionDisplay.getAntiGene());
    }

    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

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

    public Set<Publication> getPublications() {
        return publications;
    }

    public void setPublications(Set<Publication> publications) {
        this.publications = publications;
    }

    public int getNumberOfPublications() {
		if (publications == null) {
			return 0;
		} else {
			return publications.size();
		}
    }

    public boolean isImgInFigure() {
        if (noFigureOrFigureWithNoLabel())   {
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
        if (figures == null || figures.isEmpty())   {
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

    public List<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(List<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Set<GenericTerm> getExpressionTerms() {
        return expressionTerms;
    }

    public void setExpressionTerms(Set<GenericTerm> expressionTerms) {
        this.expressionTerms = expressionTerms;
    }

    public SortedMap<Publication, SortedSet<Figure>> getFiguresPerPub() {
        return figuresPerPub;
    }

    public void setFiguresPerPub(SortedMap<Publication, SortedSet<Figure>> figuresPerPub) {
        this.figuresPerPub = figuresPerPub;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public Marker getAntiGene() {
        return antiGene;
    }

    public void setAntiGene(Marker antiGene) {
        this.antiGene = antiGene;
    }
}
