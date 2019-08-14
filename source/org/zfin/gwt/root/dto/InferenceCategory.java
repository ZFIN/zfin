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
    ZFIN_MRPH_GENO("ZFIN", "ZFIN:ZDB-MRPHLNO-.*|ZFIN:ZDB-TALEN-.*|ZFIN:ZDB-CRISPR-.*|ZFIN:ZDB-GENO-.*", "(Knockdown/Genotype)"),
    ZFIN_GENE("ZFIN", "ZFIN:ZDB-GENE-.*|ZFIN:ZDB-LINCRNAG-.*|ZFIN:ZDB-LNCRNAG-.*|ZFIN:ZDB-MIRNAG-.*|ZFIN:ZDB-PIRNAG-.*|ZFIN:ZDB-NCRNAG-.*|ZFIN:ZDB-SNORNAG-.*|ZFIN:ZDB-SCRNAG-.*|ZFIN:ZDB-RRNAG-.*|ZFIN:ZDB-SRPRNAG-.*|ZFIN:ZDB-TRNAG-.*", "(Gene)"),
    UNIPROTKB("UniProtKB"),
    GO("GO"),
    GENBANK("GenBank"),
    GENPEPT("GenPept"),
    REFSEQ("RefSeq"),
    SP_KW("SP_KW"),
    UNIPROTKB_KW("UniProtKB-KW"),
    INTERPRO("InterPro"),
    EC("EC"),
    HAMAP("HAMAP"),
    SP_SL("SP_SL"),
    UNIPROTKB_SUBCELL("UniProtKB-SubCell"),
    UNIPATHWAY("UniPathway"),
    UNIRULE("UniRule"),
    ENSEMBL("Ensembl"),
    PANTHER("PANTHER",false),
    MGI("MGI"),
//    RGD("RGD"),
    WB("WB"),
    FB("FB"),
    SGD("SGD"),
    TAIR("TAIR"),
    CGD("CGD"),
    EMBL("EMBL"),
    ECOGENE("EcoGene"),
    POMBASE("PomBase"),
    XENBASE("XenBase"),
    DICTYBASE("dictyBase"),
    PROTEIN_ID("protein_id")
    ;

    private final String prefix;
    private final String match;
    private final String extraDisplay;
    private final String SEPARATOR = ":";
    private final Boolean curatable ;

    private InferenceCategory(String prefix) {
        this.prefix = prefix;
        this.match = prefix + SEPARATOR + ".*";
        this.extraDisplay = "";
        curatable = true ;
    }

    private InferenceCategory(String prefix,Boolean curatable) {
        this.prefix = prefix;
        this.match = prefix + SEPARATOR + ".*";
        this.extraDisplay = "";
        this.curatable = curatable ;
    }

    private InferenceCategory(String category, String match) {
        this.prefix = category;
        this.match = match;
        this.extraDisplay = "";
        this.curatable = true ;
    }

    private InferenceCategory(String category, String match, String extraDisplay) {
        this.prefix = category;
        this.match = match;
        this.extraDisplay = extraDisplay;
        this.curatable = true ;
    }

    public String prefix() {
        if (prefix.equals("SP_SL")) {
            return "UniProtKB-SubCell" + SEPARATOR;
        }

        if (prefix.equals("SP_KW")) {
            return "UniProtKB-KW" + SEPARATOR;
        }

        return prefix + SEPARATOR;
    }

    public String match() {
        return match;
    }

    public boolean curatable(){
        return curatable;
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
        try{
            return this == getInferenceCategoryByValue(value);
        }
        catch(Throwable t){
            return false ;
        }
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
