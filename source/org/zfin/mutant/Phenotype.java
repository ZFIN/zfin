package org.zfin.mutant;

import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.OntologyTerm;
import org.zfin.publication.Publication;

import java.util.HashSet;
import java.util.Set;

/**
 * ZFIN Domain object.
 */
public class Phenotype {


    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    private AnatomyItem anatomyTerm;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Set<Figure> figures;
    private Publication publication;
    private Term term;

    // toDo: This needs to be cleaned up when the full phenotype functionality is implemented
    private String patoSubTermzdbID;
    private String patoSuperTermzdbID;

    //private GenotypeExperiment
    private OntologyTerm termA;
    private OntologyTerm termB;
    private OntologyTerm quality;
    // ToDo: Needs to be of type Tag once the tag field in the atomic_phenotype table is being cleaned up
    private String tag;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public OntologyTerm getTermA() {
        return termA;
    }

    public void setTermA(OntologyTerm termA) {
        this.termA = termA;
    }

    public OntologyTerm getTermB() {
        return termB;
    }

    public void setTermB(OntologyTerm termB) {
        this.termB = termB;
    }

    public OntologyTerm getQuality() {
        return quality;
    }

    public void setQuality(OntologyTerm quality) {
        this.quality = quality;
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

    public String getPatoSubTermzdbID() {
        return patoSubTermzdbID;
    }

    public void setPatoSubTermzdbID(String patoSubTermzdbID) {
        this.patoSubTermzdbID = patoSubTermzdbID;
    }

    public String getPatoSuperTermzdbID() {
        return patoSuperTermzdbID;
    }

    public void setPatoSuperTermzdbID(String patoSuperTermzdbID) {
        this.patoSuperTermzdbID = patoSuperTermzdbID;
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

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public AnatomyItem getAnatomyTerm() {
        return anatomyTerm;
    }

    public void setAnatomyTerm(AnatomyItem anatomyTerm) {
        this.anatomyTerm = anatomyTerm;
        patoSuperTermzdbID = anatomyTerm.getZdbID();
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
    }

}
