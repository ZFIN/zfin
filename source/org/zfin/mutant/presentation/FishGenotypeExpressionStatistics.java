package org.zfin.mutant.presentation;

import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is a statistics class about Fish for given genotype
 */
public class FishGenotypeExpressionStatistics extends EntityStatistics {

    private Fish fish;
    private Set<Figure> figures;
    private List<FishExperiment> fishExperimentList = new ArrayList<>();

    public FishGenotypeExpressionStatistics(Fish fish) {
        this.fish = fish;
    }

    public void addFishExperiment(FishExperiment fishExperiment) {
        fishExperimentList.add(fishExperiment);
    }

    public Fish getFish() {
        return fish;
    }

    @Override
    public int getNumberOfFigures() {
        if (figures == null) {
            figures = new HashSet<>(5);
            for (FishExperiment fishExperiment : fishExperimentList)
                for (ExpressionExperiment expressionExperiment : fishExperiment.getExpressionExperiments()) {
                    if (expressionExperiment.getGene() != null)
                        figures.addAll(expressionExperiment.getAllFigures());
                }
        }
        return figures.size();
    }

    public Figure getFirstFigure() {
        if (getNumberOfFigures() == 0) {
            return null;
        } else {
            return figures.iterator().next();
        }
    }

    public boolean isImgInFigure() {
        if (isNoFigureOrFigLabel()) {
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

    public boolean isNoFigureOrFigLabel() {
        if (figures == null || figures.isEmpty()) {
            return true;
        }
        boolean thereIsFigLabel = false;
        for (Figure fig : figures) {
            if (fig.getLabel() == null) {
                thereIsFigLabel = true;
                break;
            }
        }
        return thereIsFigLabel;
    }

    @Override
    public Figure getFigure() {
        if (figures == null || figures.size() != 1)
            throw new RuntimeException("Can call this method only when there is exactly one figure");
        return figures.iterator().next();
    }

    @Override
    protected PaginationResult<Publication> getPublicationPaginationResult() {
        Set<Publication> pubs = new HashSet<>(5);
        for (FishExperiment fishExperiment : fishExperimentList)
            for (ExpressionExperiment expressionExperiment : fishExperiment.getExpressionExperiments()) {
                if (expressionExperiment.getGene() != null)
                    pubs.add(expressionExperiment.getPublication());
            }
        List<Publication> pubList = new ArrayList<>(pubs);
        return new PaginationResult<>(pubList);
    }

}
