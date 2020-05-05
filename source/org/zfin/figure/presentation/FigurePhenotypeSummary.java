package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;

import java.util.List;

public class FigurePhenotypeSummary {

    @JsonView(View.API.class) private List<Fish> fish;
    @JsonView(View.API.class) private List<SequenceTargetingReagent> sequenceTargetingReagents;
    @JsonView(View.API.class) private List<Experiment> experiments;
    @JsonView(View.API.class) private List<PostComposedEntity> entities;
    @JsonView(View.API.class) private DevelopmentStage startStage;
    @JsonView(View.API.class) private DevelopmentStage endStage;

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
