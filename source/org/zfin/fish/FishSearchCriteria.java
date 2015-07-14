package org.zfin.fish;

import org.zfin.fish.presentation.FishSearchFormBean;
import org.zfin.fish.presentation.SortBy;
import org.zfin.framework.search.AbstractSearchCriteria;
import org.zfin.framework.search.SearchCriterion;
import org.zfin.framework.search.SearchCriterionType;
import org.zfin.framework.search.SortType;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is used to contain the search criteria for antibody searches.
 */
public class FishSearchCriteria extends AbstractSearchCriteria {

    SearchCriterion geneOrFeatureNameCriteria;
    SearchCriterion phenotypeAnatomyCriteria;
    SearchCriterion featureTypeCriteria;
    SearchCriterion excludeSequenceTargetingReagentCriteria;
    SearchCriterion requireSequenceTargetingReagentCriteria;
    SearchCriterion excludeTransgenicsCriteria;
    SearchCriterion requireTransgenicsCriteria;
    SearchCriterion mutationTypeCriteria;

    private List<SearchCriterion> allCriteria;

    public FishSearchCriteria() {
    }

    //set defaults in constructor, all criteria to the list
    public FishSearchCriteria(FishSearchFormBean formBean) {
        allCriteria = new ArrayList<>();
        sort = new ArrayList<>();

        setStart(formBean.getFirstRecord() - 1); // PaginationBean is 1-based index, SearchCriteria is 0-based
        setRows(formBean.getMaxDisplayRecordsInteger());

        geneOrFeatureNameCriteria = new SearchCriterion(SearchCriterionType.GENE_OR_FEATURE_NAME, true);
        geneOrFeatureNameCriteria.setSeparator(SearchCriterion.WHITESPACE_SEPARATOR);
        geneOrFeatureNameCriteria.setValue(formBean.getGeneOrFeatureName());
        allCriteria.add(geneOrFeatureNameCriteria);

        phenotypeAnatomyCriteria = new SearchCriterion(SearchCriterionType.PHENOTYPE_ANATOMY_ID, true);
        phenotypeAnatomyCriteria.setValue(formBean.getAnatomyTermIDs());
        phenotypeAnatomyCriteria.setSeparator(",");
        phenotypeAnatomyCriteria.setNameSeparator("\\|");
        phenotypeAnatomyCriteria.setName(formBean.getAnatomyTermNames());
        allCriteria.add(phenotypeAnatomyCriteria);

        featureTypeCriteria = new SearchCriterion(SearchCriterionType.FEATURE_TYPE, false);
        featureTypeCriteria.setValue(formBean.getMutationType());
        allCriteria.add(featureTypeCriteria);

        excludeSequenceTargetingReagentCriteria = new SearchCriterion(SearchCriterionType.EXCLUDE_SEQUENCE_TARGETING_REAGENT, "false", false);
        excludeSequenceTargetingReagentCriteria.setValue(formBean.isExcludeMorphants() ? "true" : "false");
        allCriteria.add(excludeSequenceTargetingReagentCriteria);

        requireSequenceTargetingReagentCriteria = new SearchCriterion(SearchCriterionType.REQUIRE_SEQUENCE_TARGETING_REAGENT, "false", false);
        requireSequenceTargetingReagentCriteria.setValue(formBean.isMorphantsOnly() ? "true" : "false");
        allCriteria.add(requireSequenceTargetingReagentCriteria);

        excludeTransgenicsCriteria = new SearchCriterion(SearchCriterionType.EXCLUDE_TRANSGENICS, "false", false);
        excludeTransgenicsCriteria.setValue(formBean.isExcludeTransgenics() ? "true" : "false");
        allCriteria.add(excludeTransgenicsCriteria);

        requireTransgenicsCriteria = new SearchCriterion(SearchCriterionType.REQUIRE_TRANSGENICS, "false", false);
        requireTransgenicsCriteria.setValue(formBean.isTransgenicsOnly() ? "true" : "false");
        allCriteria.add(requireTransgenicsCriteria);

        mutationTypeCriteria = new SearchCriterion(SearchCriterionType.MUTATION_TYPE, false);
        if (formBean.hasMutationTypeSelected()) {
            mutationTypeCriteria.setValue(formBean.getMutationType());
        }
        allCriteria.add(mutationTypeCriteria);

        if (formBean.getSortBy().equals(SortBy.FEATURES.toString())) {
            sort.add(SortType.FISH_PARTS_COUNT_ASC);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENE_COUNT_ASC);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.FEATURES_REVERSE.toString())) {
            sort.add(SortType.FISH_PARTS_COUNT_DESC);
            sort.add(SortType.FEATURE_Z_TO_A);
            sort.add(SortType.GENE_COUNT_DESC);
            sort.add(SortType.GENE_Z_TO_A);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.GENES.toString())) {
            sort.add(SortType.GENE_COUNT_ASC);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.FISH_PARTS_COUNT_ASC);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else if (formBean.getSortBy().equals(SortBy.GENES_REVERSE.toString())) {
            sort.add(SortType.GENE_COUNT_DESC);
            sort.add(SortType.GENE_Z_TO_A);
            sort.add(SortType.FISH_PARTS_COUNT_DESC);
            sort.add(SortType.FEATURE_Z_TO_A);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        } else { // best match
            sort.add(SortType.LUCENE_SIMPLE);
            sort.add(SortType.COMPLEXITY);
            sort.add(SortType.FEATURE_TYPE);
            sort.add(SortType.GENE_A_TO_Z);
            sort.add(SortType.FEATURE_A_TO_Z);
            sort.add(SortType.GENO_UNIQUE_A_TO_Z);
        }


    }

    public List<SearchCriterion> getAllCriteria() {
        return allCriteria;
    }

    public SearchCriterion getGeneOrFeatureNameCriteria() {
        return geneOrFeatureNameCriteria;
    }

    public void setGeneOrFeatureNameCriteria(SearchCriterion geneOrFeatureNameCriteria) {
        this.geneOrFeatureNameCriteria = geneOrFeatureNameCriteria;
    }

    public SearchCriterion getPhenotypeAnatomyCriteria() {
        return phenotypeAnatomyCriteria;
    }

    public void setPhenotypeAnatomyCriteria(SearchCriterion phenotypeAnatomyCriteria) {
        this.phenotypeAnatomyCriteria = phenotypeAnatomyCriteria;
    }

    public SearchCriterion getExcludeSequenceTargetingReagentCriteria() {
        return excludeSequenceTargetingReagentCriteria;
    }

    public void setExcludeSequenceTargetingReagentCriteria(SearchCriterion excludeSequenceTargetingReagentCriteria) {
        this.excludeSequenceTargetingReagentCriteria = excludeSequenceTargetingReagentCriteria;
    }

    public SearchCriterion getRequireSequenceTargetingReagentCriteria() {
        return requireSequenceTargetingReagentCriteria;
    }

    public void setRequireSequenceTargetingReagentCriteria(SearchCriterion requireSequenceTargetingReagentCriteria) {
        this.requireSequenceTargetingReagentCriteria = requireSequenceTargetingReagentCriteria;
    }

    public SearchCriterion getExcludeTransgenicsCriteria() {
        return excludeTransgenicsCriteria;
    }

    public void setExcludeTransgenicsCriteria(SearchCriterion excludeTransgenicsCriteria) {
        this.excludeTransgenicsCriteria = excludeTransgenicsCriteria;
    }

    public SearchCriterion getRequireTransgenicsCriteria() {
        return requireTransgenicsCriteria;
    }

    public void setRequireTransgenicsCriteria(SearchCriterion requireTransgenicsCriteria) {
        this.requireTransgenicsCriteria = requireTransgenicsCriteria;
    }


    public boolean isFilterSearchOnly() {
        return !geneOrFeatureNameCriteria.hasValues() && !phenotypeAnatomyCriteria.hasValues();
    }

    public SearchCriterion getMutationTypeCriteria() {
        return mutationTypeCriteria;
    }

    public void setMutationTypeCriteria(SearchCriterion mutationTypeCriteria) {
        this.mutationTypeCriteria = mutationTypeCriteria;
    }

}
