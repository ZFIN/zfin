package org.zfin.sequence;

import javax.persistence.*;

@Entity
@Table(name = "FOREIGN_DB")
public class ForeignDB implements Comparable<ForeignDB> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdb_db_pk_id")
    private Long dbID;
    @Column(name = "fdb_db_name")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.ForeignDB$AvailableName")})
    private AvailableName dbName;
    @Column(name = "fdb_db_query")
    private String dbUrlPrefix;
    @Column(name = "fdb_url_suffix")
    private String dbUrlSuffix;
    @Column(name = "fdb_db_significance")
    private Integer significance;
    @Column(name = "fdb_db_display_name")
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

    public String getDisplayName () {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int compareTo(ForeignDB other) {
        if (other == null)
            return 1;
        if (other.getSignificance() == null)
            return 1;
        if (significance == null)
            return -1;
        if (significance.compareTo(other.getSignificance()) != 0)
            return significance.compareTo(other.getSignificance());
        return dbName.compareTo(other.getDbName());
    }


    public static enum AvailableName {
        ADDGENE("Addgene"),
        CREZOO("CreZoo"),
        CURATED_MIRNA_MATURE("Curated miRNA Mature"),
        CURATED_MIRNA_STEM_LOOP("Curated miRNA Stem Loop"),
        DBSNP("dbSNP"),
        EBI_CELL("EBI-Cell"),
        EC("EC"),
        ENSEMBL("Ensembl"),
        ENSEMBL_GRCZ10_("Ensembl(GRCz10)"),
        ENSEMBL_CLONE("Ensembl_Clone"),
        ENSEMBL_TRANS("Ensembl_Trans"),
        ENSEMBL_SNP("Ensembl_SNP"),
        FLYBASE("FLYBASE"),
        FLYBASE_ANATOMY("FLYBASE-Anatomy"),
        GENBANK("GenBank"),
        GENE("Gene"),
        GENPEPT("GenPept"),
        GEO("GEO"),
        HAMAP("HAMAP"),
        HGNC("HGNC"),
        INTERPRO("InterPro"),
        MIRBASE_MATURE("miRBASE Mature"),
        MESH("MESH"),
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
        PMID("PMID"),
        PUBPROT("PUBPROT"),
        PUBRNA("PUBRNA"),
        QUICKGO("QuickGO"),
        REFSEQ("RefSeq"),
        RRID("RRID"),
        SANGER_CLONE("Sanger_Clone"),
        // FB case 8239
        //SP_KW("SP_KW"),
        SIGNAFISH("SignaFish"),
        UNIPROTKB_KW("UniProtKB-KW"),
        //SP_SL("SP_SL"),
        UNIPROTKB_SUBCELL("UniProtKB-SubCell"),
        UNIPATHWAY("UniPathway"),
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
        ZFIN_PROT("ZFIN_PROT"),
        ZFISHBOOK("zfishbook"),
        ZFISHBOOK_CONSTRUCTS("zfishbook-constructs"),
        HTTP("HTTP"),
        HTTPS("HTTPS"),
        ZFIN("ZFIN"),
        ZFA("ZFA"),
        WIKIPEDIA("Wikipedia"),
        UBERON("UBERON"),
        ISBN("ISBN"),
        ZMP("ZMP"),
        CRISPRZ("CRISPRz"),
        UNIRULE("UniRule");


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

    public boolean isZfishbook () {
        if (displayName != null && displayName.equals("zfishbook"))
			   return true;

	    return false;
    }
    public boolean isCrezoo () {
        if (displayName != null && displayName.equals("CreZoo"))
            return true;

        return false;
    }
    public boolean isZmp () {
        if (displayName != null && displayName.equals("ZMP"))
			   return true;

	    return false;
    }
}
