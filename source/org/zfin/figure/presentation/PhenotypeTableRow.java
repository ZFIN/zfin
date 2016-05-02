package org.zfin.figure.presentation;


import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.PhenotypeWarehouse;


/* Should be: Genotype, Experiment, Start Stage, End Stage, PhenotypeStatement  */
public class PhenotypeTableRow {

    private FishExperiment fishExperiment;
    private Fish fish;
    private Experiment experiment;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private String fishNameOrder;
    private PhenotypeStatementWarehouse phenotypeStatement;

    public PhenotypeTableRow(PhenotypeStatementWarehouse phenotypeStatement) {
        this.phenotypeStatement = phenotypeStatement;
        PhenotypeWarehouse phenotypeWarehouse = phenotypeStatement.getPhenotypeWarehouse();
        fishExperiment = phenotypeWarehouse.getFishExperiment();
        fish = phenotypeWarehouse.getFishExperiment().getFish();
        experiment = phenotypeWarehouse.getFishExperiment().getExperiment();
        start = phenotypeWarehouse.getStart();
        end = phenotypeWarehouse.getEnd();
        fishNameOrder = fish.getAbbreviationOrder();
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

    public Experiment getExperiment() {
        return experiment;
    }


    public DevelopmentStage getStart() {
        return start;
    }


    public DevelopmentStage getEnd() {
        return end;
    }

    public PhenotypeStatementWarehouse getPhenotypeStatement() {
        return phenotypeStatement;
    }

    public String getFishNameOrder() {
        return fishNameOrder;
    }

}
