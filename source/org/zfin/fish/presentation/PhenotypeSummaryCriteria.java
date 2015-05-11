package org.zfin.fish.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Figure;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.mutant.ConstructSearchCriteria;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.presentation.Construct;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import java.util.ArrayList;
import java.util.List;


public class PhenotypeSummaryCriteria {

    private Marker gene;
    private GenotypeExperiment genotypeExperiment;

    private Antibody antibody;
    private PostComposedEntity entity;
    private Term singleTermEitherPosition;
    private DevelopmentStage start;
    private DevelopmentStage end;


    private boolean isWildtypeOnly;
    private boolean withImagesOnly;
    private boolean isStandardEnvironment;
    private boolean isChemicalEnvironment;


    public PhenotypeSummaryCriteria clone() {
        PhenotypeSummaryCriteria criteria = new PhenotypeSummaryCriteria();

        criteria.setGene(gene);
        criteria.setGenotypeExperiment(genotypeExperiment);
        criteria.setGenotype(genotype);

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

    public GenotypeExperiment getGenotypeExperiment() {
        return genotypeExperiment;
    }

    public void setGenotypeExperiment(GenotypeExperiment genotypeExperiment) {
        this.genotypeExperiment = genotypeExperiment;
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
    private Figure figure;

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public Construct getConstruct() {

        return construct;
    }

    public void setConstruct(Construct construct) {
        this.construct = construct;
    }

    private MartFish fish;
    private Construct construct;
    private List<GenotypeExperiment> genotypeExperiments;
    private FishSearchCriteria criteria;
    private ConstructSearchCriteria constructCriteria;
    private Genotype genotype;

    public ConstructSearchCriteria getConstructCriteria() {
        return constructCriteria;
    }

    public void setConstructCriteria(ConstructSearchCriteria constructCriteria) {
        this.constructCriteria = constructCriteria;
    }

    public MartFish getFish() {

        return fish;
    }

    public void setFish(MartFish fish) {
        this.fish = fish;
    }

    public List<GenotypeExperiment> getGenotypeExperiments() {
        return genotypeExperiments;
    }

    public void setGenotypeExperiments(List<GenotypeExperiment> genotypeExperiments) {
        this.genotypeExperiments = genotypeExperiments;
    }

    public FishSearchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(FishSearchCriteria criteria) {
        this.criteria = criteria;
    }

    public List<ZfinEntity> getSearchCriteriaPhenotype() {
        if (criteria == null || !criteria.getPhenotypeAnatomyCriteria().hasValues())
            return null;
        List<String> values = criteria.getPhenotypeAnatomyCriteria().getValues();
        List<String> names = criteria.getPhenotypeAnatomyCriteria().getNames();
        List<ZfinEntity> terms = new ArrayList<ZfinEntity>(values.size());
        for (int index = 0; index < values.size(); index++) {
            ZfinEntity term = new ZfinEntity();
            term.setID(values.get(index));
            term.setName(names.get(index));
            terms.add(term);
        }
        return terms;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Genotype getGenotype() {
        return genotype;
    }
}
