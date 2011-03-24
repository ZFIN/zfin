package org.zfin.mutant;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Figure;

import java.util.Date;
import java.util.Set;

/**
 * Business object that describes a phenotype experiment, i.e. an experiment
 * that has phenotypic data annotated to (phenotype statement).
 *
 */
public class PhenotypeExperiment {
    private long id;
    private GenotypeExperiment genotypeExperiment;
    private DevelopmentStage startStage;
    private DevelopmentStage endStage;
    private Figure figure;
    private Date dateCreated;
    private Date dateLastModified;

    private Set<PhenotypeStatement> phenotypeStatements;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
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

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Set<PhenotypeStatement> getPhenotypeStatements() {
        return phenotypeStatements;
    }

    public void setPhenotypeStatements(Set<PhenotypeStatement> phenotypeStatements) {
        this.phenotypeStatements = phenotypeStatements;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateLastModified() {
        return dateLastModified;
    }

    public void setDateLastModified(Date dateLastModified) {
        this.dateLastModified = dateLastModified;
    }

    @Override
    public String toString() {
        return "PhenotypeExperiment{" +
                "id=" + id +
                ", genotypeExperiment=" + genotypeExperiment +
                ", startStage=" + startStage +
                ", endStage=" + endStage +
                ", figure=" + figure +
                ", phenotypeStatements=" + phenotypeStatements +
                '}';
    }
}
