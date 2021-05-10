package org.zfin.framework.search;

/**
 * These should match field & filter names in our searches,
 * back end classes will use these types to build queries.
 */
public enum SearchCriterionType {

    GENE_OR_FEATURE_NAME("Gene or Feature"),
    PHENOTYPE_ANATOMY_ID("Phenotype Anatomy ID"),
    FEATURE_TYPE("Feature Type"),
    EXCLUDE_SEQUENCE_TARGETING_REAGENT("Exclude Sequence Targeting Reagent"),
    REQUIRE_SEQUENCE_TARGETING_REAGENT("Require Sequence Targeting Reagent"),
    EXCLUDE_TRANSGENICS("Exclude Transgenics"),
    REQUIRE_TRANSGENICS("Require Transgenics"),
    MUTATION_TYPE("Mutation Type"),
    CONSTRUCT_NAME("Construct"),
    PROMOTER_GENE("Promoter Gene"),
    EXPRESSED_GENE("Expressed Gene"),
    ENGINEERED_REGION("Engineered Region"),
    AFFECTED_GENE("Affected Genomic Region"),
    LINE_AVAILABLE("Lines available"),
    CONSTRUCT_TYPE("Construct Type");

    String name;

    private SearchCriterionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
