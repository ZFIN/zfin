package org.zfin.mutant;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;

import java.io.Serializable;


public class GenotypeFigure implements Serializable {

    private int id;
    private Genotype genotype;
    private Figure figure;
    private GenericTerm superTerm;
    private GenericTerm subTerm;
    private GenericTerm qualityTerm;
    private String tag;
    private Marker sequenceTargetingReagent;
    private PhenotypeExperiment phenotypeExperiment;

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

    public GenericTerm getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(GenericTerm superTerm) {
        this.superTerm = superTerm;
    }

    public GenericTerm getSubTerm() {
        return subTerm;
    }

    public void setSubTerm(GenericTerm subTerm) {
        this.subTerm = subTerm;
    }

    public GenericTerm getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(GenericTerm qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public PhenotypeExperiment getPhenotypeExperiment() {
        return phenotypeExperiment;
    }

    public void setPhenotypeExperiment(PhenotypeExperiment phenotypeExperiment) {
        this.phenotypeExperiment = phenotypeExperiment;
    }
}
