package org.zfin.expression;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;


public class ExpressionSummaryCriteria {

    private Marker gene;
    private GenotypeExperiment genotypeExperiment;
    private Genotype genotype;
    private Antibody antibody;
    private PostComposedEntity entity;
    private Term singleTermEitherPosition;
    private DevelopmentStage start;
    private DevelopmentStage end;
    private Figure figure;

    private boolean isWildtypeOnly;
    private boolean withImagesOnly;
    private boolean isStandardEnvironment;
    private boolean isChemicalEnvironment;


    public ExpressionSummaryCriteria clone() {
        ExpressionSummaryCriteria criteria = new ExpressionSummaryCriteria();

        criteria.setGene(gene);
        criteria.setGenotypeExperiment(genotypeExperiment);
        criteria.setGenotype(genotype);
        criteria.setAntibody(antibody);
        criteria.setEntity(entity);
        criteria.setSingleTermEitherPosition(singleTermEitherPosition);
        criteria.setStart(start);
        criteria.setEnd(end);
        criteria.setFigure(figure);
        criteria.setWildtypeOnly(isWildtypeOnly);
        criteria.setWithImagesOnly(withImagesOnly);
        criteria.setStandardEnvironment(isStandardEnvironment);

        return criteria;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
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
}
