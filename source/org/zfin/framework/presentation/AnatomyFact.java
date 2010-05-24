package org.zfin.framework.presentation;

import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.marker.Marker;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.io.Serializable;

/**
 * Class that maps to a statistics table for antibodies
 */
public class AnatomyFact implements Serializable {

    private long zdbID;
    ExpressionResult expressionResult;
    Term superterm;
    Term subterm;
    Marker gene;
    Figure figure;
    Image image;
    Publication publication;
    String type;


    public long getZdbID() {
        return zdbID;
    }

    public void setZdbID(long zdbID) {
        this.zdbID = zdbID;
    }

    public Term getSuperterm() {
        return superterm;
    }

    public void setSuperterm(Term superterm) {
        this.superterm = superterm;
    }

    public Term getSubterm() {
        return subterm;
    }

    public void setSubterm(Term subterm) {
        this.subterm = subterm;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExpressionResult getExpressionResult() {
        return expressionResult;
    }

    public void setExpressionResult(ExpressionResult expressionResult) {
        this.expressionResult = expressionResult;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}