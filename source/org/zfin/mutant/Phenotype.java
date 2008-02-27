package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;
import org.zfin.ontology.OntologyTerm;
import org.zfin.publication.Publication;

import java.util.Set;

/**
 * ZFIN Domain object. 
 */
public class Phenotype {

    public enum Tag{
        NORMAL("normal"),
        ABNORMAL("abnormal");

        private String value;

        Tag(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }
    }


    private String zdbID;
    private GenotypeExperiment genotypeExperiment;
    // toDo: This needs to be cleaned up when the full phenotype functionality is implemented
    private String patoEntityAzdbID;
    private String patoEntityBzdbID;

    //private GenotypeExperiment
    private OntologyTerm termA;
    private OntologyTerm termB;
    private OntologyTerm quality;
    private DevelopmentStage stage;
    // ToDo: Needs to be of type Tag once the tag field in the atomic_phenotype table is being cleaned up
    private String tag;
    private Set<Figure> figures;
    private Publication publication;
    private Term term;

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

    public DevelopmentStage getStage() {
        return stage;
    }

    public void setStage(DevelopmentStage stage) {
        this.stage = stage;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPatoEntityAzdbID() {
        return patoEntityAzdbID;
    }

    public void setPatoEntityAzdbID(String patoEntityAzdbID) {
        this.patoEntityAzdbID = patoEntityAzdbID;
    }

    public String getPatoEntityBzdbID() {
        return patoEntityBzdbID;
    }

    public void setPatoEntityBzdbID(String patoEntityBzdbID) {
        this.patoEntityBzdbID = patoEntityBzdbID;
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
}
