package org.zfin.fish.presentation;

import org.zfin.fish.FishSearchCriteria;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;

import java.util.ArrayList;
import java.util.List;


public class PhenotypeSummaryCriteria {

    private Fish fish;
    private List<GenotypeExperiment> genotypeExperiments;
    private FishSearchCriteria criteria;
    private Genotype genotype;

    public Fish getFish() {
        return fish;
    }

    public void setFish(Fish fish) {
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
