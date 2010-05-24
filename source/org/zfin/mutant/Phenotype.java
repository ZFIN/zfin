package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * ZFIN Domain object.
 */
public class Phenotype {


    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    private Term superterm;
    private Term subterm;
    // quality term
    private Term term;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Figure> figures;
    private Publication publication;

    //private GenotypeExperiment
    // ToDo: Needs to be of type Tag once the tag field in the atomic_phenotype table is being cleaned up
    private String tag;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Term getSubterm() {
        return subterm;
    }

    public void setSubterm(Term subterm) {
        this.subterm = subterm;
    }

    public Term getSuperterm() {
        return superterm;
    }

    public void setSuperterm(Term superterm) {
        this.superterm = superterm;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }


    public Set<Figure> getFigures() {
        return figures;
    }

    public void setFigures(Set<Figure> figures) {
        this.figures = figures;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public void addFigure(Figure figure) {
        if (figures == null)
            figures = new HashSet<Figure>(4);
        figures.add(figure);
    }

    public void removeFigure(Figure figure) {
        if(figures != null)
            figures.remove(figure);
    }

    public static enum Tag {
        NORMAL("normal"),
        ABNORMAL("abnormal");

        private String value;

        Tag(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Tag getTagFromName(String name) {
            for (Tag tag : values()) {
                if (tag.value.equals(name))
                    return tag;
            }
            throw new RuntimeException("No Tag object with name '" + name + "' found.");
        }
    }


}
