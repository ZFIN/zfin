package org.zfin.mutant;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GoTerm;
import org.zfin.ontology.Term;
import org.zfin.publication.Publication;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ZFIN Domain object.
 */
public abstract class Phenotype {


    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    private AnatomyItem anatomySuperTerm;
    private GoTerm goSuperTerm;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Figure> figures;
    private Publication publication;
    private Term term;

    //private GenotypeExperiment
    // ToDo: Needs to be of type Tag once the tag field in the atomic_phenotype table is being cleaned up
    private String tag;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public GoTerm getGoSuperTerm() {
        return goSuperTerm;
    }

    public void setGoSuperTerm(GoTerm goSuperTerm) {
        this.goSuperTerm = goSuperTerm;
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

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    abstract public Term getSubTerm();

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public AnatomyItem getAnatomySuperTerm() {
        return anatomySuperTerm;
    }

    public void setAnatomySuperTerm(AnatomyItem anatomySuperTerm) {
        this.anatomySuperTerm = anatomySuperTerm;
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

    public Term getSuperterm() {
        if(anatomySuperTerm != null)
            return anatomySuperTerm;
        else
            return goSuperTerm;
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
