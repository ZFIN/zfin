package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.PostComposedEntity;

/**
 * Stores a collection of entities used to display one row of the figureview expression table
 */
@Setter
@Getter
public class ExpressionTableRow {

    @JsonView(View.FigureAPI.class)
    private Marker gene;
    @JsonView(View.FigureAPI.class)
    private Antibody antibody;
    private FishExperiment fishExperiment;
    @JsonView(View.FigureAPI.class)
    private Fish fish;
    @JsonView(View.FigureAPI.class)
    private Experiment experiment;
    @JsonView(View.FigureAPI.class)
    private String qualifier;
    private Boolean isExpressionFound;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage start;
    @JsonView(View.FigureAPI.class)
    private DevelopmentStage end;
    @JsonView(View.FigureAPI.class)
    private PostComposedEntity entity;
    @JsonView(View.FigureAPI.class)
    private ExpressionAssay assay;
    private String fishNameOrder;
    @JsonView(View.FigureAPI.class)
    private Figure figure;


    //this is a key used for deciding whether to repeat the genotype in the display tag
    // (it's a "new" genotype if it's a new gene and the same genotype...)
    private String geneGenoxZdbIDs;

    public ExpressionTableRow() {

    }

    public ExpressionTableRow(ExpressionResult expressionResult) {
        ExpressionExperiment expressionExperiment = expressionResult.getExpressionExperiment();
        setGene(expressionExperiment.getGene());
        setAntibody(expressionExperiment.getAntibody());
        setFishExperiment(expressionExperiment.getFishExperiment());
        setFish(expressionExperiment.getFishExperiment().getFish());
        setExperiment(expressionExperiment.getFishExperiment().getExperiment());
        setStart(expressionResult.getStartStage());
        setEnd(expressionResult.getEndStage());
        setIsExpressionFound(expressionResult.isExpressionFound());
        if (!expressionResult.isExpressionFound()) {
            setQualifier("Not Detected");
        }
        setEntity(expressionResult.getEntity());
        setAssay(expressionResult.getExpressionExperiment().getAssay());

        setGeneGenoxZdbIDs(gene.getZdbID() + fishExperiment.getZdbID());

        //todo: might want this to be getNameOrder later...
        setFishNameOrder(fishExperiment.getFish().getAbbreviationOrder());


    }


    public String getGeneGenoxZdbIDs() {
        return geneGenoxZdbIDs;
    }

    public void setGeneGenoxZdbIDs(String geneGenoxZdbIDs) {
        this.geneGenoxZdbIDs = geneGenoxZdbIDs;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    @JsonView(View.FigureAPI.class)
    @JsonProperty("id")
    public String getUniqueKey() {
        StringBuilder builder = new StringBuilder();
        builder.append(gene.getZdbID());
        if (antibody != null)
            builder.append(antibody.getZdbID());
        if (fish != null)
            builder.append(fish.getZdbID());
        if (start != null)
            builder.append(start.getZdbID());
        if (end != null)
            builder.append(end.getZdbID());
        return builder.toString();
    }
}
