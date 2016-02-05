package org.zfin.mutant;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;

import java.io.Serializable;


public class GenotypeFigure implements Serializable {

    private int id;
    private Genotype genotype;
    private Fish fish;
    private Figure figure;
    private String tag;
    private Marker sequenceTargetingReagent;
    private PhenotypeExperiment phenotypeExperiment;
    private PhenotypeStatementWarehouse phenotypeStatement;
    private FishExperiment fishExperiment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Marker getSequenceTargetingReagent() {
        return sequenceTargetingReagent;
    }

    public void setSequenceTargetingReagent(Marker sequenceTargetingReagent) {
        this.sequenceTargetingReagent = sequenceTargetingReagent;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public PhenotypeExperiment getPhenotypeExperiment() {
        return phenotypeExperiment;
    }

    public void setPhenotypeExperiment(PhenotypeExperiment phenotypeExperiment) {
        this.phenotypeExperiment = phenotypeExperiment;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public PhenotypeStatementWarehouse getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatementWarehouse phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }
}
