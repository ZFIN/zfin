package org.zfin.expression.presentation;

import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public class ExpressionDisplay implements Comparable<ExpressionDisplay> {
    private Marker expressedGene;
    private Set<Publication> publications;
    private Set<Figure> figures;
    private List<ExpressionResult> expressionResults;
    private Experiment experiment;
    private Set<GenericTerm> expressionTerms;

    public ExpressionDisplay(Marker expressedGene) {
        this .expressedGene = expressedGene;
    }

    public int compareTo(ExpressionDisplay anotherExpressionDisplay) {
        if (expressedGene == null){
            return -1;
        }
        else if (expressedGene.compareTo(anotherExpressionDisplay.getExpressedGene()) == 0)
            return experiment.compareTo(anotherExpressionDisplay.getExperiment());
        return expressedGene.compareTo(anotherExpressionDisplay.getExpressedGene());
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

    public Marker getExpressedGene() {
        return expressedGene;
    }

    public void setExpressedGene(Marker expressedGene) {
        this.expressedGene = expressedGene;
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
}
