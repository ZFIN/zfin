package org.zfin.sequence;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "FOREIGN_DB")
public class ForeignDB implements Comparable<ForeignDB>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fdb_db_pk_id")
    private Long dbID;
    @JsonView(View.SequenceDetailAPI.class)
    @Column(name = "fdb_db_name")
    @org.hibernate.annotations.Type(type = "org.zfin.framework.StringEnumValueUserType",
            parameters = {@org.hibernate.annotations.Parameter(name = "enumClassname", value = "org.zfin.sequence.ForeignDB$AvailableName")})
    private AvailableName dbName;
    @Column(name = "fdb_db_query")
    @JsonView(View.MarkerRelationshipAPI.class)
    private String dbUrlPrefix;
    @Column(name = "fdb_url_suffix")
    @JsonView(View.MarkerRelationshipAPI.class)
    private String dbUrlSuffix;
    @Column(name = "fdb_db_significance")
    private Integer significance;
    @JsonView(View.MarkerRelationshipAPI.class)
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

    public String getDisplayName() {
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
        ABREGISTRY("ABRegistry"),
        CREZOO("CreZoo"),
        CURATED_MIRNA_MATURE("Curated miRNA Mature"),
        CURATED_MIRNA_STEM_LOOP("Curated miRNA Stem Loop"),
        DBSNP("dbSNP"),
        EBI_CELL("EBI-Cell"),
        EC("EC"),
        ENSEMBL("Ensembl"),
        ENSEMBL_GRCZ11_("Ensembl(GRCz11)"),
        ENSEMBL_CLONE("Ensembl_Clone"),
        ENSEMBL_TRANS("Ensembl_Trans"),
        ENSEMBL_SNP("Ensembl_SNP"),
        EXPRESSIONATLAS("ExpressionAtlas"),
        FLYBASE("FLYBASE"),
        FISHMIRNA("FishMiRNA"),
        FISHMIRNA_EXPRESSION("FishMiRNA-Expression"),
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
        NCBO_CARO("NCBO-CARO"),
        NTR_REGION("NTR-Region"),
        NOCTUA("Noctua"),
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
        UNIPROTKB("UniProtKB"),
        UNISTS("UniSTS"),
        UNRELEASEDRNA("UnreleasedRNA"),
        VEGA("VEGA"),
        VEGA_CLONE("VEGA_Clone"),
        VEGA_TRANS("Vega_Trans"),
        VEGA_WITHDRAWN("Vega_Withdrawn"),
        VEGAPROT("VEGAPROT"),
        WASHUZ("WashUZ"),
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
        UNIRULE("UniRule"),
        AGR_GENE("Alliance"),
        AGR_DISEASE("Alliance"),
        CZRC("CZRC"),
        PDB("PDB"),
        RNA_CENTRAL("RNACentral"),
        ZIRC_PROTOCOL("ZIRCProtocol");


        private final String value;

        @JsonValue
        public String getValue() {
            return value;
        }

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

    public boolean isZfishbook() {
        return displayName != null && displayName.equals(AvailableName.ZFISHBOOK.name());
    }

    public boolean isFishMiRNA() {
        return dbName != null && dbName == AvailableName.FISHMIRNA;
    }

    public boolean isFishMiRNAExpression() {
        return dbName != null && dbName == AvailableName.FISHMIRNA_EXPRESSION;
    }

    public boolean isCrezoo() {
        return displayName != null && displayName.equals(AvailableName.CREZOO.name());
    }

    public boolean isZmp() {
        return displayName != null && displayName.equals(AvailableName.ZMP.name());
    }
}
