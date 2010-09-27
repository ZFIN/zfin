package org.zfin.gwt.marker.ui;

/**
 * This is just a hack, we don't use most of these and they aren't mapped.
 * When we share these, we can get rid of this.
 */
public enum MarkerRelationshipEnumTypeGWTHack {
    CLONE_CONTAINS_GENE("clone contains gene"),
    CLONE_CONTAINS_SMALL_SEGMENT("clone contains small segment"),
    CLONE_CONTAINS_TRANSCRIPT("clone contains transcript"),
    CLONE_OVERLAP("clone overlap"),
    CODING_SEQUENCE_OF("coding sequence of"),
    CONTAINS_ENGINEERED_REGION("contains engineered region"),
    CONTAINS_POLYMORPHISM("contains polymorphism"),
    GENE_CONTAINS_SMALL_SEGMENT("gene contains small segment"),
    GENE_PRODUCES_TRANSCRIPT("gene produces transcript"),
    GENE_ENCODES_SMALL_SEGMENT("gene encodes small segment"),
    GENE_HAS_ARTIFACT("gene has artifact"),
    GENE_HYBRIDIZED_BY_SMALL_SEGMENT("gene hybridized by small segment"),
    GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY("gene product recognized by antibody"),
    KNOCKDOWN_REAGENT_TARGETS_GENE("knockdown reagent targets gene"),
    TRANSCRIPT_TARGETS_GENE("transcript targets gene"),
    PROMOTER_OF("promoter of"),
    ;

    private final String value;

    MarkerRelationshipEnumTypeGWTHack(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

}
