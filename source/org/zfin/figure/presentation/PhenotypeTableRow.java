package org.zfin.figure.presentation;


import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.mutant.*;


/* Should be: Genotype, Experiment, Start Stage, End Stage, PhenotypeStatement  */
public class PhenotypeTableRow{

    private FishExperiment fishExperiment;
    private Fish fish;
    private Experiment experiment;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private PhenotypeStatement phenotypeStatement;
    private String fishNameOrder;

    public PhenotypeTableRow() {

    }

    public PhenotypeTableRow(PhenotypeStatement phenotypeStatement) {
        setFishExperiment(phenotypeStatement.getPhenotypeExperiment().getFishExperiment());
        setFish(phenotypeStatement.getPhenotypeExperiment().getFishExperiment().getFish());
        setExperiment(phenotypeStatement.getPhenotypeExperiment().getFishExperiment().getExperiment());
        setStart(phenotypeStatement.getPhenotypeExperiment().getStartStage());
        setEnd(phenotypeStatement.getPhenotypeExperiment().getEndStage());
        setPhenotypeStatement(phenotypeStatement);

        //todo: might want this to be getNameOrder later...
        setFishNameOrder(fishExperiment.getFish().getAbbreviationOrder());


    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
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

    public String getFishNameOrder() {
        return fishNameOrder;
    }

    public void setFishNameOrder(String fishNameOrder) {
        this.fishNameOrder = fishNameOrder;
    }
}
