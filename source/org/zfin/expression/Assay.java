package org.zfin.expression;

/**
 * Domain object Enumeration.
 */
public enum Assay {

    ANY("Any"),
    WESTERN_BLOT("Western blot"),
    IMMUNOHISTOCHEMISTRY("Immunohistochemistry"),
    OTHER("other"),
    CDNA_CLONES("cDNA clones");

    private String name;

    private Assay(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Assay getAsssay(String assayName) {
        for (Assay t : values()) {
            if (t.getName().equals(assayName))
                return t;
        }
        throw new RuntimeException("No Assay name " + assayName + " found.");
    }

}
