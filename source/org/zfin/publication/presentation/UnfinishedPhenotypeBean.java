package org.zfin.publication.presentation;

import org.zfin.expression.Figure;
import org.zfin.mutant.PhenotypeExperiment;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class UnfinishedPhenotypeBean {

    private List<PhenotypeExperiment> phenotypeExperiments;

    public List<PhenotypeExperiment> getPhenotypeExperiments() {
        return phenotypeExperiments;
    }

    public void setPhenotypeExperiments(List<PhenotypeExperiment> phenotypeExperiments) {
        this.phenotypeExperiments = phenotypeExperiments;
    }

    public Set<Figure> getDistinctFigures() {
        if (phenotypeExperiments == null)
            return null;
        // order by label
        Set<Figure> figures = new TreeSet<Figure>();
        for (PhenotypeExperiment phenotypeExperiment : phenotypeExperiments) {
            figures.add(phenotypeExperiment.getFigure());
        }
        return figures;
    }
}
