package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;


public class ExpressionSummaryCriteria {

    private Marker gene;
    private FishExperiment fishExperiment;
    private Genotype genotype;
    private Fish fish;
    private Antibody antibody;
    private SequenceTargetingReagent sequenceTargetingReagent;
    private PostComposedEntity entity;
    private Term singleTermEitherPosition;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private Figure figure;

    private boolean isWildtypeOnly;
    private boolean withImagesOnly;
    private boolean isStandardEnvironment;
    private boolean isChemicalEnvironment;
    private boolean isHeatShockEnvironment;
    private boolean showCondition = true;


    public ExpressionSummaryCriteria clone() {
        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();

        criteria.setGene(gene);
        criteria.setFishExperiment(fishExperiment);
        criteria.setGenotype(genotype);
        criteria.setFish(fish);
        criteria.setAntibody(antibody);
        criteria.setEntity(entity);
        criteria.setSingleTermEitherPosition(singleTermEitherPosition);
        criteria.setStart(start);
        criteria.setEnd(end);
        criteria.setFigure(figure);
        criteria.setWildtypeOnly(isWildtypeOnly);
        criteria.setWithImagesOnly(withImagesOnly);
        criteria.setStandardEnvironment(isStandardEnvironment);
        criteria.setChemicalEnvironment(isChemicalEnvironment);

        return criteria;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Antibody getAntibody() {
        return antibody;
    }

    public void setAntibody(Antibody antibody) {
        this.antibody = antibody;
    }

    public boolean isWildtypeOnly() {
        return isWildtypeOnly;
    }

    public void setWildtypeOnly(boolean wildtypeOnly) {
        isWildtypeOnly = wildtypeOnly;
    }

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public Term getSingleTermEitherPosition() {
        return singleTermEitherPosition;
    }

    public void setSingleTermEitherPosition(Term singleTermEitherPosition) {
        this.singleTermEitherPosition = singleTermEitherPosition;
    }

    public boolean isWithImagesOnly() {
        return withImagesOnly;
    }

    public void setWithImagesOnly(boolean withImagesOnly) {
        this.withImagesOnly = withImagesOnly;
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

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public boolean isStandardEnvironment() {
        return isStandardEnvironment;
    }

    public void setStandardEnvironment(boolean standardEnvironment) {
        isStandardEnvironment = standardEnvironment;
    }

    public boolean isChemicalEnvironment() {
        return isChemicalEnvironment;
    }

    public void setChemicalEnvironment(boolean chemicalEnvironment) {
        isChemicalEnvironment = chemicalEnvironment;
    }

    public SequenceTargetingReagent getSequenceTargetingReagent() {
        return sequenceTargetingReagent;
    }

    public void setSequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        this.sequenceTargetingReagent = sequenceTargetingReagent;
    }

    public boolean isShowCondition() {
        return showCondition;
    }

    public void setShowCondition(boolean showCondition) {
        this.showCondition = showCondition;
    }

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public boolean isHeatShockEnvironment() {
        return isHeatShockEnvironment;
    }

    public void setHeatShockEnvironment(boolean heatShockEnvironment) {
        isHeatShockEnvironment = heatShockEnvironment;
    }
}
