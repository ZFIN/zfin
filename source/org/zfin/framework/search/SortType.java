package org.zfin.framework.search;

/**
 * Should hold all of the sort options to be passed to the back end,
 * These should be named for what they are in the interface, and it's
 * up to the backend code to translate what they mean with respect
 * to the specific circumstances.
 *
 * (IE, for antibody search GENE_ASC might be the antigen gene,
 * but for expression search it might be the expressed gene.)
 */
public enum SortType {
    GENO_UNIQUE_A_TO_Z("genotype unique name asc"),
    GENE_A_TO_Z("gene asc"),
    GENE_Z_TO_A("gene desc"),
    GENE_COUNT_ASC("gene count asc"),
    GENE_COUNT_DESC("gene count desc"),
    FISH_PARTS_COUNT_ASC("fish parts count asc"),
    FISH_PARTS_COUNT_DESC("fish parts count desc"),
    FEATURE_A_TO_Z("feature asc"),
    FEATURE_Z_TO_A("feature desc"),
    COMPLEXITY("complexity"),
    FEATURE_TYPE("feature type"),
    LUCENE_SIMPLE("lucene simple"),
    LUCENE_RAW("lucene raw");

    String name;

    private SortType(String name) {
        this.name = name;
    }



}
