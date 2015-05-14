package org.zfin.mutant;

import org.zfin.expression.Experiment;

import java.util.Set;

/**
 * Fish Model entity
 */
public class FishModel {

    private long ID;
    private Fish fish;
    private Experiment experiment;
    private boolean standard;
    private boolean standardOrGenericControl;
    private Set<DiseaseModel> diseaseModelSet;

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

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
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

    public Set<DiseaseModel> getDiseaseModelSet() {
        return diseaseModelSet;
    }

    public void setDiseaseModelSet(Set<DiseaseModel> diseaseModelSet) {
        this.diseaseModelSet = diseaseModelSet;
    }
}
