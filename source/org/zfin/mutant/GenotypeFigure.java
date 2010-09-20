package org.zfin.mutant;

import org.zfin.expression.Figure;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;

import java.io.Serializable;


public class GenotypeFigure implements Serializable {

    private int id;
    private Genotype genotype;
    private Figure figure;
    private Term superTerm;
    private Term subTerm;
    private Term qualityTerm;
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

    public Term getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(Term superTerm) {
        this.superTerm = superTerm;
    }

    public Term getSubTerm() {
        return subTerm;
    }

    public void setSubTerm(Term subTerm) {
        this.subTerm = subTerm;
    }

    public Term getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(Term qualityTerm) {
        this.qualityTerm = qualityTerm;
    }
}
