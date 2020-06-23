package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.api.View;

import java.util.Objects;
import java.util.Set;

/**
 * Domain object.
 */
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
    private Set<ExpressionExperiment> expressionExperiments;
    private Set<GeneGenotypeExperiment> geneGenotypeExperiments;
    private Set<DiseaseAnnotationModel> diseaseAnnotationModels;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public boolean isStandard() {
        return standard;
    }

    public void setStandard(boolean standard) {
        this.standard = standard;
    }

    public boolean isStandardOrGenericControl() {
        return standardOrGenericControl;
    }

    public void setStandardOrGenericControl(boolean standardOrGenericControl) {
        this.standardOrGenericControl = standardOrGenericControl;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public Set<ExpressionExperiment> getExpressionExperiments() {
        return expressionExperiments;
    }

    public void setExpressionExperiments(Set<ExpressionExperiment> expressionExperiments) {
        this.expressionExperiments = expressionExperiments;
    }

    public Set<PhenotypeExperiment> getPhenotypeExperiments() {
        return phenotypeExperiments;
    }

    public void setPhenotypeExperiments(Set<PhenotypeExperiment> phenotypeExperiments) {
        this.phenotypeExperiments = phenotypeExperiments;
    }

    public Set<DiseaseAnnotationModel> getDiseaseAnnotationModels() {
        return diseaseAnnotationModels;
    }

    public void setDiseaseAnnotationModels(Set<DiseaseAnnotationModel> diseaseAnnotationModels) {
        this.diseaseAnnotationModels = diseaseAnnotationModels;
    }

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
        if (totalCount == 0 && getExperiment().getExperimentConditions().size() >= 2 )
            return true;

        return false;
    }
}
