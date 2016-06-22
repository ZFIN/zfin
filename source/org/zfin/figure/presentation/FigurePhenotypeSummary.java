package org.zfin.figure.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;

import java.util.List;

public class FigurePhenotypeSummary {

    private List<Fish> fish;
    private List<SequenceTargetingReagent> sequenceTargetingReagents;
    private List<Experiment> experiments;
    private List<PostComposedEntity> entities;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;

    public List<Fish> getFish() {
        return fish;
    }

    public void setFish(List<Fish> fish) {
        this.fish = fish;
    }

    public List<SequenceTargetingReagent> getSequenceTargetingReagents() {
        return sequenceTargetingReagents;
    }

    public void setSequenceTargetingReagents(List<SequenceTargetingReagent> sequenceTargetingReagents) {
        this.sequenceTargetingReagents = sequenceTargetingReagents;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }

    public List<PostComposedEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<PostComposedEntity> entities) {
        this.entities = entities;
    }

    public DevelopmentStage getStartStage() {
        return startStage;
    }

    public void setStartStage(DevelopmentStage startStage) {
        this.startStage = startStage;
    }

    public DevelopmentStage getEndStage() {
        return endStage;
    }

    public void setEndStage(DevelopmentStage endStage) {
        this.endStage = endStage;
    }
}
