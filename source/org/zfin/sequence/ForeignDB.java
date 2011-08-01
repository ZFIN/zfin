package org.zfin.sequence;

/**

 */
public class ForeignDB implements Comparable<ForeignDB> {

    private Long dbID;
    private AvailableName dbName;
    private String dbUrlPrefix;
    private String dbUrlSuffix;
    private Integer significance;
    private String displayName;

    public Long getDbID() {
        return dbID;
    }

    public void setDbID(Long dbID) {
        this.dbID = dbID;
    }

    public String getDbUrlPrefix() {
        return dbUrlPrefix;
    }

    public void setDbUrlPrefix(String dbUrlPrefix) {
        this.dbUrlPrefix = dbUrlPrefix;
    }

    public String getDbUrlSuffix() {
        return dbUrlSuffix;
    }

    public void setDbUrlSuffix(String dbUrlSuffix) {
        this.dbUrlSuffix = dbUrlSuffix;
    }

    public Integer getSignificance() {
        return significance;
    }

    public void setSignificance(Integer significance) {
        this.significance = significance;
    }

    public AvailableName getDbName() {
        return dbName;
    }

    public void setDbName(AvailableName dbName) {
        this.dbName = dbName;
    }

    public String isDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int compareTo(ForeignDB other) {
        if (other == null)
            return 1;
        if (significance.compareTo(other.getSignificance()) != 0)
            return significance.compareTo(other.getSignificance());
        return dbName.compareTo(other.getDbName());
    }


    public static enum AvailableName {
        CURATED_MIRNA_MATURE("Curated miRNA Mature"),
        CURATED_MIRNA_STEM_LOOP("Curated miRNA Stem Loop"),
        DBSNP("dbSNP"),
        EBI_CELL("EBI-Cell"),
        EC("EC"),
        ENSEMBL("Ensembl"),
        ENSEMBL_ZV9_("Ensembl(Zv9)"),
        ENSEMBL_CLONE("Ensembl_Clone"),
        ENSEMBL_TRANS("Ensembl_Trans"),
        ENSEMBL_SNP("Ensembl_SNP"),
        FLYBASE("FLYBASE"),
        FLYBASE_ANATOMY("FLYBASE-Anatomy"),
        GDB("GDB"),
        GENBANK("GenBank"),
        GENE("Gene"),
        GENPEPT("GenPept"),
        GEO("GEO"),
        HAMAP("HAMAP"),
        INTERPRO("InterPro"),
        MIRBASE_MATURE("miRBASE Mature"),
        MIRBASE_STEM_LOOP("miRBASE Stem Loop"),
        MGI("MGI"),
        MGI_ANATOMY("MGI-Anatomy"),
        MICROCOSM("MicroCosm"),
        MODB("MODB"),
        NCBO_CARO("NCBO-CARO"),
        NOVELGENE("NovelGene"),
        OMIM("OMIM"),
        PANTHER("PANTHER"),
        PREVEGA("PREVEGA"),
        PROSITE("PROSITE"),
        PFAM("Pfam"),
        PREENSEMBL_ZV7_("PreEnsembl(Zv7)"),
        PUBMED("PubMed"),
        PUBPROT("PUBPROT"),
        PUBRNA("PUBRNA"),
        QUICKGO("QuickGO"),
        REFSEQ("RefSeq"),
        SANGER_CLONE("Sanger_Clone"),
        SGD("SGD"),
        SP_KW("SP_KW"),
        SP_SL("SP_SL"),
        TAO("TAO"),
        UNIGENE("UniGene"),
        UNIPROTKB("UniProtKB"),
        UNISTS("UniSTS"),
        UNRELEASEDRNA("UnreleasedRNA"),
        VEGA("VEGA"),
        VEGA_CLONE("VEGA_Clone"),
        VEGA_TRANS("Vega_Trans"),
        VEGA_WITHDRAWN("Vega_Withdrawn"),
        VEGAPROT("VEGAPROT"),
        WASHU("WashU"),
        WASHUZ("WashUZ"),
        ZF_ESPRESSO("ZF-Espresso"),
        ZFIN_PROT("ZFIN_PROT"),;


        private final String value;

        private AvailableName(String type) {
            this.value = type;
        }


        public String toString() {
            return this.value;
        }

        public static AvailableName getType(String type) {
            for (AvailableName t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No ForeignDB named " + type + " found.");
        }
    }
}
