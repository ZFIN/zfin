package org.zfin.framework.search;

/**
 * These should match field & filter names in our searches,
 * back end classes will use these types to build queries.
 */
public enum SearchCriterionType {

    GENE_OR_FEATURE_NAME("Gene or Feature"),
    PHENOTYPE_ANATOMY_ID("Phenotype Anatomy ID"),
    FEATURE_TYPE("Feature Type"),
    EXCLUDE_MORPHOLINOS("Exclude Morpholinos"),
    REQUIRE_MORPHOLNOS("Require Morpholinos"),
    EXCLUDE_TRANSGENICS("Exclude Transgenics"),
    REQUIRE_TRANSGENICS("Require Transgenics"),
    MUTATION_TYPE("Mutation Type");

    String name;

    private SearchCriterionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
