package org.zfin.mutant.presentation;

import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.EntityStatistics;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.mutant.*;
import org.zfin.publication.Publication;

import java.util.*;

/**
 * This class is a statistics class about Fish for given genotype
 */
public class FishGenotypePhenotypeStatistics extends EntityStatistics {

    private Fish fish;
    private Set<Figure> figures;
    private List<FishExperiment> fishExperimentList = new ArrayList<>();

    public FishGenotypePhenotypeStatistics(Fish fish) {
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
                for (PhenotypeExperiment phenotype : fishExperiment.getPhenotypeExperiments()) {
                    for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                        figures.add(phenotype.getFigure());
                    }
                }
        }
        return figures.size();
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
            for (PhenotypeExperiment phenotype : fishExperiment.getPhenotypeExperiments()) {
                for (PhenotypeStatement phenoStatement : phenotype.getPhenotypeStatements()) {
                    pubs.add(phenotype.getFigure().getPublication());
                }
            }
        List<Publication> pubList = new ArrayList<>(pubs);
        return new PaginationResult<>(pubList);
    }

    public String getTargetGeneOrder() {
        return null;
    }

}
