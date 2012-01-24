package org.zfin.framework.presentation;

public enum MatchingTextType {

    AFFECTED_GENE_NAME("Affected Gene Name"),
    GENE_NAME("Gene Name"),
    MORPHOLINO_NAME("Morpholino Name"),
    MORPHOLINO_ALIAS("Morpholino Alias [Morpholino]"),
    GENE_ABBREVIATION("Gene Symbol"),
    AFFECTED_GENE_ABBREVIATION("Affected Gene Symbol"),
    CONSTRUCT_ABBREVIATION("Construct Symbol"),
    CONSTRUCT_NAME("Construct Name"),
    CONSTRUCT_ALIAS("Construct Alias [Construct]"),
    GENE_ALIAS("Gene Prev. Name"),
    AFFECTED_GENE_ALIAS("Previous Name [Affected Gene]"),
    FEATURE_NAME("Genomic Feature Name"),
    FEATURE_ABBREVIATION("Genomic Feature Abbreviation"),
    FEATURE_ALIAS("Genomic Feature Alias [Feature]"),
    ANTIBODY_NAME("Name"),
    ANTIBODY_ALIAS("Alias [Antibody]"),
    AO_TERM("Anatomy Term"),
    CONSTRUCT("Construct"),
    RELATED_MARKER("Related Marker"),
    RELATED_MARKER_ABBREVIATION("Related Marker Symbol [Marker]"),
    RELATED_MARKER_NAME("Related Marker Name [Marker]"),
    RELATED_MARKER_ALIAS("Related Marker Alias [Marker]"),
    TRANSGENIC("Transgenic"),
    NOT_TRANSGENIC("Not a Transgenic"),
    MORPHANT("Morphant"),
    NOT_MORPHANT("Not a Morphant"),
    MUTATION_TYPE("Mutation Type"),
    PART_OF("part of");

    private String name;

    private MatchingTextType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
