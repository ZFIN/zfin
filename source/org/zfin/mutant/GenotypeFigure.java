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
    private Marker morpholino;

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

    public Marker getMorpholino() {
        return morpholino;
    }

    public void setMorpholino(Marker morpholino) {
        this.morpholino = morpholino;
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
}
