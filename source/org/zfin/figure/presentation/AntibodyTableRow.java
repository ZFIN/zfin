package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.framework.api.View;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.PostComposedEntity;


@Setter
@Getter
/*
Should represent these columns:
Antibody, Assay, Genotype, Experiment, Start Stage, End Stage, SuperTerm, SubTerm
*/
public class AntibodyTableRow {

    @JsonView(View.FigureAPI.class)
    private Antibody antibody;
    @JsonView(View.FigureAPI.class)
    private ExpressionAssay assay;
    private FishExperiment fishExperiment;
    @JsonView(View.FigureAPI.class)
    private Fish fish;
    @JsonView(View.FigureAPI.class)
    private Experiment experiment;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage start;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage end;
    @JsonView(View.FigureAPI.class)
    private String qualifier;
    private Boolean isExpressionFound;
    @JsonView(View.FigureAPI.class)
    private PostComposedEntity entity;
    private String fishNameOrder;


    //this is a key used for deciding whether to repeat the antibody in the display tag
    // (it's a "new" antibody if it's a new antibody and the same genotype...)
    private String antibodyGenoxZdbIDs;


    public AntibodyTableRow(ExpressionFigureStage figureStage, ExpressionResult2 expressionResult) {
        ExpressionExperiment2 expressionExperiment = figureStage.getExpressionExperiment();
        setAntibody(expressionExperiment.getAntibody());
        setAssay(figureStage.getExpressionExperiment().getAssay());
        setFishExperiment(expressionExperiment.getFishExperiment());
        setFish(fishExperiment.getFish());
        setExperiment(fishExperiment.getExperiment());
        setStart(figureStage.getStartStage());
        setEnd(figureStage.getEndStage());
        setEntity(expressionResult.getEntity());
        setAntibodyGenoxZdbIDs(antibody.getZdbID() + fishExperiment.getZdbID());
        setExpressionFound(expressionResult.isExpressionFound());
        if (!expressionResult.isExpressionFound()) {
            setQualifier("Not Detected");
        }

        //todo: might want this to be getNameOrder later...
        setFishNameOrder(fishExperiment.getFish().getAbbreviationOrder());
    }

    public Boolean getExpressionFound() {
        return isExpressionFound;
    }

    public void setExpressionFound(Boolean expressionFound) {
        isExpressionFound = expressionFound;
    }

}
