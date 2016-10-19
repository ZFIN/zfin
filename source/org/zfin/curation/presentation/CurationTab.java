package org.zfin.curation.presentation;

public enum CurationTab {

    CONSTRUCT("construct", "Construct"),
    FEATURE("feature", "Feature"),
    FISH("fish", "Fish"),
    EXPERIMENT("experiment", "Experiment"),
    FX("fx", "FX"),
    PHENO("pheno", "PHENO"),
    GO("go", "GO"),
    DISEASE("disease", "Disease"),
    ORTHOLOGY("orthology", "Orthology "),;

    private String name;
    private String displayName;

    CurationTab(String name, String displayName) {
        this.displayName = displayName;
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    public static CurationTab getTab(String name) {
        for (CurationTab tab : values()) {
            if (tab.getName().equals(name))
                return tab;
        }
        return null;
    }
}
