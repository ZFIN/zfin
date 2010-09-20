package org.zfin.expression.presentation;

import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.List;
import java.util.Set;

public class ExpressionDisplay implements Comparable<ExpressionDisplay> {
    private Marker expressedGene;
    private Set<Publication> publications;
    private Set<Figure> figures;
    private boolean moInExperiment;
    private List<ExpressionResult> expressionResults;
    private Set<Term> nonDuplicatedTerms;

    public int compareTo(ExpressionDisplay anotherExpressionDisplay) {
        if (expressedGene == null)
            return -1;
        return expressedGene.compareTo(anotherExpressionDisplay.getExpressedGene());
    }

    public Set<Term> getNonDuplicatedTerms() {
		return nonDuplicatedTerms;
    }

    public void setNonDuplicatedTerms(Set<Term> nonDuplicatedTerms) {
		this.nonDuplicatedTerms = nonDuplicatedTerms;
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

    public boolean isMoInExperiment() {
        return moInExperiment;
    }

    public void setMoInExperiment(boolean moInExperiment) {
        this.moInExperiment = moInExperiment;
    }

    public boolean isImgInFigure() {
        if (figures == null || figures.size() == 0)
            return false;
        for (Figure fig : figures) {
           if (fig.getImages() != null && fig.getImages().size() > 0)
			   return true;
        }
        return false;
    }

    public Figure getSingleFig() {
		if (figures == null || figures.size() == 0)
		  return null;

		for (Figure fig : figures)
          return fig;

        return null;
    }

    public List<ExpressionResult> getExpressionResults() {
        return expressionResults;
    }

    public void setExpressionResults(List<ExpressionResult> expressionResults) {
        this.expressionResults = expressionResults;
    }
}