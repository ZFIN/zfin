package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * ZFIN Domain object.
 */
public class Phenotype implements Comparable<Phenotype> {


    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    private GenericTerm superterm;
    private GenericTerm subterm;
    // quality term
    private GenericTerm qualityTerm;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Figure> figures;
    private Publication publication;

    // ToDo: Needs to be of type Tag once the tag field in the atomic_phenotype table is being cleaned up
    private String tag;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public GenericTerm getSuperterm() {
        return superterm;
    }

    public void setSuperterm(GenericTerm superterm) {
        this.superterm = superterm;
    }

    public GenericTerm getSubterm() {
        return subterm;
    }

    public void setSubterm(GenericTerm subterm) {
        this.subterm = subterm;
    }

    public GenericTerm getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(GenericTerm qualityTerm) {
        this.qualityTerm = qualityTerm;
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
        if (figures != null)
            figures.remove(figure);
    }

    @Override
    public int compareTo(Phenotype o) {
        String o1GenotypeName = getGenotypeExperiment().getGenotype().getName();
        final String o2GenotypeName = o.getGenotypeExperiment().getGenotype().getName();
        if (!o1GenotypeName.equals(o2GenotypeName))
            return o1GenotypeName.compareTo(o2GenotypeName);
        return 0;

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
