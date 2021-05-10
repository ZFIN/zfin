package org.zfin.figure.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.ontology.PostComposedEntity;

/*
Should represent these columns:
Antibody, Assay, Genotype, Experiment, Start Stage, End Stage, SuperTerm, SubTerm
*/
public class AntibodyTableRow {

    private Antibody antibody;
    private ExpressionAssay assay;
    private FishExperiment fishExperiment;
    private Fish fish;
    private Experiment experiment;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private String qualifier;
    private Boolean isExpressionFound;
    private PostComposedEntity entity;
    private String fishNameOrder;



    //this is a key used for deciding whether to repeat the antibody in the display tag
    // (it's a "new" antibody if it's a new antibody and the same genotype...)
    private String antibodyGenoxZdbIDs;


    public AntibodyTableRow() {

    }

    public AntibodyTableRow(ExpressionResult expressionResult) {
        ExpressionExperiment expressionExperiment = expressionResult.getExpressionExperiment();
        setAntibody(expressionExperiment.getAntibody());
        setAssay(expressionResult.getExpressionExperiment().getAssay());
        setFishExperiment(expressionExperiment.getFishExperiment());
        setFish(fishExperiment.getFish());
        setExperiment(fishExperiment.getExperiment());
        setStart(expressionResult.getStartStage());
        setEnd(expressionResult.getEndStage());
        setEntity(expressionResult.getEntity());
        setAntibodyGenoxZdbIDs(antibody.getZdbID()+ fishExperiment.getZdbID());
        setExpressionFound(expressionResult.isExpressionFound());
        if (!expressionResult.isExpressionFound()) {
            setQualifier("Not Detected");
        }

        //todo: might want this to be getNameOrder later...
        setFishNameOrder(fishExperiment.getFish().getAbbreviationOrder());

    }

    public String getAntibodyGenoxZdbIDs() {
        return antibodyGenoxZdbIDs;
    }

    public void setAntibodyGenoxZdbIDs(String antibodyGenoxZdbIDs) {
        this.antibodyGenoxZdbIDs = antibodyGenoxZdbIDs;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public ExpressionAssay getAssay() {
        return assay;
    }

    public void setAssay(ExpressionAssay assay) {
        this.assay = assay;
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

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
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

    public String getFishNameOrder() {
        return fishNameOrder;
    }

    public void setFishNameOrder(String fishNameOrder) {
        this.fishNameOrder = fishNameOrder;
    }
}
