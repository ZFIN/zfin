package org.zfin.mutant.presentation;

import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.presentation.MartFish;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.FishExperiment;

import java.util.ArrayList;
import java.util.List;


public class ConstructExpressionSummaryCriteria {

    private MartFish fish;
    private List<FishExperiment> fishExperiments;
    private FishSearchCriteria criteria;
    private Genotype genotype;

    public MartFish getFish() {
        return fish;
    }

    public void setFish(MartFish fish) {
        this.fish = fish;
    }

    public List<FishExperiment> getGenotypeExperiments() {
        return fishExperiments;
    }

    public void setGenotypeExperiments(List<FishExperiment> fishExperiments) {
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
