package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment2;
import org.zfin.framework.api.View;

import java.util.Objects;
import java.util.Set;

@Setter
@Getter
public class FishExperiment implements Comparable<FishExperiment> {

    @JsonView(View.API.class)
    private String zdbID;
    private boolean standard;
    private boolean standardOrGenericControl;
    @JsonView(View.API.class)
    private Experiment experiment;
    @JsonView(View.API.class)
    private Fish fish;
    private Set<PhenotypeExperiment> phenotypeExperiments;
    private Set<ExpressionExperiment2> expressionExperiments;
    private Set<GeneGenotypeExperiment> geneGenotypeExperiments;
    private Set<DiseaseAnnotationModel> diseaseAnnotationModels;

    @Override
    public int compareTo(FishExperiment o) {
        int fishCompare = fish.compareTo(o.getFish());
        if (fishCompare != 0) {
            return fishCompare;
        }
        return experiment.compareTo(o.getExperiment());
    }

    public Set<GeneGenotypeExperiment> getGeneGenotypeExperiments() {
        return geneGenotypeExperiments;
    }

    public void setGeneGenotypeExperiments(Set<GeneGenotypeExperiment> geneGenotypeExperiments) {
        this.geneGenotypeExperiments = geneGenotypeExperiments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!getClass().isAssignableFrom(o.getClass()) ||
            o.getClass().isAssignableFrom(getClass())) {
            return false;
        }
        FishExperiment that = (FishExperiment) o;
        return Objects.equals(this.getExperiment(), that.getExperiment()) &&
               Objects.equals(this.getFish(), that.getFish());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getExperiment(), this.getFish());
    }

    public boolean isAmelioratedOrExacerbated() {
        Long totalCount = getFish().getFishFunctionalAffectedGeneCount() + getFish().getFishPhenotypicConstructCount();

        if (totalCount >= 2)
            return true;
        if (totalCount == 1 && !isStandardOrGenericControl())
            return true;
        if (totalCount == 0 && getExperiment().getExperimentConditions().size() >= 2)
            return true;

        return false;
    }

    public boolean isTwoChangesInEnvironment() {
        return getExperiment().getExperimentConditions().size() == 2;
    }
}
