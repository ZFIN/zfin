package org.zfin.figure.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.PostComposedEntity;

/**
 * Stores a collection of entities used to display one row of the figureview expression table
 */
public class ExpressionTableRow{
    private Marker gene;
    private Antibody antibody;
    private FishExperiment fishExperiment;
    private Fish fish;
    private Experiment experiment;
    private String qualifier;
    private Boolean isExpressionFound;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private PostComposedEntity entity;
    private ExpressionAssay assay;
    private String fishNameOrder;


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
        setExpressionFound(expressionResult.isExpressionFound());
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

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
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

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public Boolean getExpressionFound() {
        return isExpressionFound;
    }

    public void setExpressionFound(Boolean expressionFound) {
        isExpressionFound = expressionFound;
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

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public ExpressionAssay getAssay() {
        return assay;
    }

    public void setAssay(ExpressionAssay assay) {
        this.assay = assay;
    }

    public String getFishNameOrder() {
        return fishNameOrder;
    }

    public void setFishNameOrder(String fishNameOrder) {
        this.fishNameOrder = fishNameOrder;
    }
}
