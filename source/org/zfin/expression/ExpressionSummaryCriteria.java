package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;


@Setter
@Getter
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

    public boolean isWildtypeOnly() {
        return isWildtypeOnly;
    }

    public void setWildtypeOnly(boolean wildtypeOnly) {
        isWildtypeOnly = wildtypeOnly;
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

    public boolean isHeatShockEnvironment() {
        return isHeatShockEnvironment;
    }

    public void setHeatShockEnvironment(boolean heatShockEnvironment) {
        isHeatShockEnvironment = heatShockEnvironment;
    }
}
