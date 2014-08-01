package org.zfin.figure.presentation;

import org.apache.commons.collections.CollectionUtils;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.figure.service.FigureViewService;
import org.zfin.fish.FishAnnotation;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

/*
Should represent these columns:
Antibody, Assay, Genotype, Experiment, Start Stage, End Stage, SuperTerm, SubTerm
*/
public class AntibodyTableRow {

    private Antibody antibody;
    private ExpressionAssay assay;
    private GenotypeExperiment genotypeExperiment;
    private Genotype genotype;
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
        setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
        setGenotype(genotypeExperiment.getGenotype());
        setExperiment(genotypeExperiment.getExperiment());
        setStart(expressionResult.getStartStage());
        setEnd(expressionResult.getEndStage());
        setEntity(expressionResult.getEntity());
        setAntibodyGenoxZdbIDs(antibody.getZdbID()+genotypeExperiment.getZdbID());
        setExpressionFound(expressionResult.isExpressionFound());
        if (!expressionResult.isExpressionFound())
            setQualifier("Not Detected");

/*
        if (CollectionUtils.isNotEmpty(genotypeExperiment.getGenotypeExperimentFishAnnotations())) {
            FishAnnotation fish = genotypeExperiment.getGenotypeExperimentFishAnnotations().iterator().next().getFishAnnotation();
            //todo: needs to be zero-padded
            setFishNameOrder(fish.getName());
        } else {
*/
        setFishNameOrder(FigureViewService.buildFishNameOrder(genotypeExperiment));
//        }

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

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
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
