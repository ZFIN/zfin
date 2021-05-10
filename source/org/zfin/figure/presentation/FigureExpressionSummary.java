package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;

import java.util.List;

public class FigureExpressionSummary {

    @JsonView(View.API.class) private List<Marker> genes;
    @JsonView(View.API.class) private List<Marker> antibodies;
    @JsonView(View.API.class) private List<Fish> fish;
    @JsonView(View.API.class) private List<SequenceTargetingReagent> sequenceTargetingReagents;
    @JsonView(View.API.class) private List<Experiment> experiments;
    @JsonView(View.API.class) private List<PostComposedEntity> entities;
    @JsonView(View.API.class) private DevelopmentStage startStage;
    @JsonView(View.API.class) private DevelopmentStage endStage;
    @JsonView(View.API.class) private Clone probe;
    private List<OrganizationLink> probeSuppliers;

    public List<Marker> getGenes() {
        return genes;
    }

    public void setGenes(List<Marker> genes) {
        this.genes = genes;
    }

    public List<Marker> getAntibodies() {
        return antibodies;
    }

    public void setAntibodies(List<Marker> antibodies) {
        this.antibodies = antibodies;
    }

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

    public Clone getProbe() {
        return probe;
    }

    public void setProbe(Clone probe) {
        this.probe = probe;
    }

    public List<OrganizationLink> getProbeSuppliers() {
        return probeSuppliers;
    }

    public void setProbeSuppliers(List<OrganizationLink> probeSuppliers) {
        this.probeSuppliers = probeSuppliers;
    }
}
