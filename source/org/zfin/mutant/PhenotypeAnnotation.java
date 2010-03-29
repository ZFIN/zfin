package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * ZFIN Domain object for phenotype annotations.
 */
public class PhenotypeAnnotation {

    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Figure> figures;
    private Publication publication;
    private GenericTerm superTerm;
    private GenericTerm subTerm;
    private GenericTerm qualityTerm;
    private String tag;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
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

    public GenericTerm getSuperTerm() {
        return superTerm;
    }

    public void setSuperTerm(GenericTerm superTerm) {
        this.superTerm = superTerm;
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
            figures = new HashSet<Figure>();
        figures.add(figure);
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