package org.zfin.fish.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Figure;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.presentation.Construct;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import java.util.ArrayList;
import java.util.List;


public class PhenotypeSummaryCriteria {

    private Marker gene;
    private FishExperiment fishExperiment;

    private Antibody antibody;
    private PostComposedEntity entity;
    private Term singleTermEitherPosition;
    private DevelopmentStage start;
    private DevelopmentStage end;

    private Fish fish;
    private Construct construct;
    private List<FishExperiment> fishExperiments;
    private FishSearchCriteria criteria;
    private Genotype genotype;


    private boolean isWildtypeOnly;
    private boolean withImagesOnly;
    private boolean isStandardEnvironment;
    private boolean isChemicalEnvironment;




    public PhenotypeSummaryCriteria clone() {
        PhenotypeSummaryCriteria criteria = new PhenotypeSummaryCriteria();

        criteria.setGene(gene);
        criteria.setFishExperiment(fishExperiment);
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

    public FishExperiment getFishExperiment() {
        return fishExperiment;
    }

    public void setFishExperiment(FishExperiment fishExperiment) {
        this.fishExperiment = fishExperiment;
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

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
        this.fish = fish;
    }

    public List<FishExperiment> getFishExperiments() {
        return fishExperiments;
    }

    public void setFishExperiments(List<FishExperiment> fishExperiments) {
        this.fishExperiments = fishExperiments;
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
