package org.zfin.gwt.root.dto;


/**
 * Categories.
 * <p/>
 * Note that for the current evidence codes, ZFIN GENEs are always included in ZFIN_ANY
 * and the two are never included at the same time.
 * <p/>
 * The first parameter is the category that gets added to the term when entered into the inference list.
 * The second parameter is the match parameter so we know if the inference category is a gene versus a
 * morpholino or genotype (e.g.)
 * The third parameter is extra display text.
 */
public enum InferenceCategory {
    ZFIN_MRPH_GENO("ZFIN", "ZFIN:ZDB-MRPHLNO-.*|ZFIN:ZDB-GENO-.*", "(Morpholino/Genotype)"),
    ZFIN_GENE("ZFIN", "ZFIN:ZDB-GENE-.*", "(Gene)"),
    UNIPROTKB("UniProtKB"),
    GO("GO"),
    GENBANK("GenBank"),
    GENPEPT("GenPept"),
    REFSEQ("RefSeq"),
    SP_KW("SP_KW"),
    INTERPRO("InterPro"),
    EC("EC");

    private final String prefix;
    private final String match;
    private final String extraDisplay;
    private final String SEPARATOR = ":";

    private InferenceCategory(String prefix) {
        this.prefix = prefix;
        this.match = prefix + SEPARATOR + ".*";
        this.extraDisplay = "";
    }

    private InferenceCategory(String category, String match) {
        this.prefix = category;
        this.match = match;
        this.extraDisplay = "";
    }

    private InferenceCategory(String category, String match, String extraDisplay) {
        this.prefix = category;
        this.match = match;
        this.extraDisplay = extraDisplay;
    }

    public String prefix() {
        return prefix + SEPARATOR;
    }

    public String match() {
        return match;
    }

    @Override
    public String toString() {
        if (extraDisplay == null || extraDisplay.isEmpty()) {
            return this.prefix;
        } else {
            return this.prefix + " " + extraDisplay;
        }
    }

    public boolean isType(String value) {
        return this == getInferenceCategoryByValue(value);
    }

    public static boolean isType(InferenceCategory inferenceCategory, String value) {
        return inferenceCategory == getInferenceCategoryByValue(value);
    }

    public static InferenceCategory getInferenceCategoryByValue(String value) {
        for (InferenceCategory t : values()) {
            if (value.trim().matches(t.match())) {
                return t;
            }
        }
        throw new RuntimeException("No InferenceCategory with matching value " + value + " found.");
    }

    public static InferenceCategory getInferenceCategoryByName(String name) {
        for (InferenceCategory t : values()) {
            if (name.trim().equals(t.name())) {
                return t;
            }
        }
        throw new RuntimeException("No InferenceCategory named " + name + " found.");
    }

}
