package org.zfin.figure.presentation;


import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.PhenotypeStatementWarehouse;
import org.zfin.mutant.PhenotypeWarehouse;


/* Should be: Genotype, Experiment, Start Stage, End Stage, PhenotypeStatement  */
@Setter
@Getter
public class PhenotypeTableRow {

    private FishExperiment fishExperiment;
    @JsonView(View.FigureAPI.class)
    private Fish fish;
    @JsonView(View.FigureAPI.class)
    private Experiment experiment;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage start;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage end;
    private String fishNameOrder;
    @JsonView(View.FigureAPI.class)
    private PhenotypeStatementWarehouse phenotypeStatement;
    @JsonView(View.FigureAPI.class)
    private Figure figure;

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

}
