package org.zfin.figure.presentation;


import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.mutant.*;
import org.zfin.mutant.repository.PhenotypeRepository;

import java.util.Date;
import java.util.Set;



/* Should be: Genotype, Experiment, Start Stage, End Stage, PhenotypeStatement  */
public class PhenotypeTableRow{
    private GenotypeExperiment genotypeExperiment;
    private Genotype genotype;
    private Experiment experiment;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private PhenotypeStatement phenotypeStatement;

    public PhenotypeTableRow() {

    }

    public PhenotypeTableRow(PhenotypeStatement phenotypeStatement) {
        setGenotypeExperiment(phenotypeStatement.getPhenotypeExperiment().getGenotypeExperiment());
        setGenotype(phenotypeStatement.getPhenotypeExperiment().getGenotypeExperiment().getGenotype());
        setExperiment(phenotypeStatement.getPhenotypeExperiment().getGenotypeExperiment().getExperiment());
        setStart(phenotypeStatement.getPhenotypeExperiment().getStartStage());
        setEnd(phenotypeStatement.getPhenotypeExperiment().getEndStage());
        setPhenotypeStatement(phenotypeStatement);
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public DevelopmentStage getStart() {
        return start;
    }

    public void setStart(DevelopmentStage start) {
        this.start = start;
    }

    public DevelopmentStage getEnd() {
        return end;
    }

    public void setEnd(DevelopmentStage end) {
        this.end = end;
    }

    public PhenotypeStatement getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public void setPhenotypeStatement(PhenotypeStatement phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
    }
}
