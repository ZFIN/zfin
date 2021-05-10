package org.zfin.framework.presentation;

public enum MatchingTextType {

    AFFECTED_GENE_NAME("Affected Genomic Region Name"),
    GENE_NAME("Gene Name"),
    SEQUENCE_TARGETING_REAGENT_NAME("Sequence Targeting Reagent Name"),
    SEQUENCE_TARGETING_REAGENT_ALIAS("Sequence Targeting Reagent Alias [Sequence Targeting Reagent]"),
    GENE_ABBREVIATION("Gene Symbol"),
    AFFECTED_GENE_ABBREVIATION("Affected Genomic Region Symbol"),
    CONSTRUCT_ABBREVIATION("Construct Symbol"),
    CONSTRUCT_NAME("Construct Name"),
    CONSTRUCT_ALIAS("Construct Alias [Construct]"),
    GENE_ALIAS("Gene Prev. Name"),
    AFFECTED_GENE_ALIAS("Previous Name [Affected Genomic Region]"),
    FEATURE_NAME("Genomic Feature Name"),
    FEATURE_LINE_NUMBER("Genomic Feature Line Number"),
    FEATURE_ABBREVIATION("Genomic Feature Abbreviation"),
    FEATURE_ALIAS("Genomic Feature Alias [Feature]"),
    ANTIBODY_NAME("Name"),
    ANTIBODY_ALIAS("Alias [Antibody]"),
    AO_TERM("Anatomy Term"),
    GO_TERM("GO Term"),
    CONSTRUCT("Construct"),
    RELATED_MARKER("Related Marker"),
    RELATED_MARKER_ABBREVIATION("Related Marker to Construct [Marker Symbol]"),
    RELATED_MARKER_NAME("Related Marker To Construct [Marker Name]"),
    RELATED_MARKER_ALIAS("Related Marker to Construct [Marker Alias]"),
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
