package org.zfin.framework.presentation;

public enum NavigationMenuOptions {
    ABSTRACT("Abstract"),
    ANTIBODIES("Antibodies"),
    DIRECTLY_ATTRIBUTED_DATA("Directly Attributed Data"),
    DISEASE("Human Disease / Model"),
    EFGs("Engineered Foreign Genes"),
    ERRATA("Errata / Notes"),
    EXPRESSION("Expression"),
    FIGURES("Figures"),
    FISH("Fish"),
    GENES("Genes / Markers"),
    MAPPING("Mapping"),
    MUTATION("Mutations / Transgenics"),
    ORTHOLOGY("Orthology"),
    PHENOTYPE("Phenotype"),
    STRS("Sequence Targeting Reagents"),
    SUMMARY("Summary"),
    ZEBRASHARE("Zebrashare Submission Details");

    public final String value;

    private NavigationMenuOptions(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }

}
