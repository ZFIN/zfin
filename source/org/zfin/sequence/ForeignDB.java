package org.zfin.sequence;

/**

 */
public class ForeignDB {
    private String dbName ;
    private String dbUrlPrefix;
    private String dbUrlSuffix;
    private String significance;


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

    public String getSignificance() {
        return significance;
    }

    public void setSignificance(String significance) {
        this.significance = significance;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public static enum AvailableName {

//        ARRAY_EXPRESS("ArrayExpress"),
        BLAST("BLAST"),
        BLASTP("BLASTP"),
        BLAT("BLAT"),
        DBSNP("dbSNP"),
        EBI_CELL("EBI-Cell"),
        EC("EC"),
        ENSEMBL("ENSEMBL"),
        ENSEMBL_ZV7("Ensembl(Zv7)"),
        ENSEMBL_CLONE("Ensembl_Clone"),
        ENSEMBL_SNP("Ensembl_SNP"),
        ENTREZ_GENE("Entrez Gene"),
        FLYBASE("FLYBASE"),
        FLYBASE_ANATOMY("FLYBASE-Anatomy"),
        GDB("GDB"),
        GENBANK("GenBank"),
        GENPEPT("GenPept"),
        GEO("GEO"),
        INTVEGA("INTVEGA"),
        INTERPRO("InterPro"),
        MEGA_BLAST("MEGA BLAST"),
        MGI("MGI"),
        MGI_ANATOMY("MGI-Anatomy"),
        MODB("MODB"),
        NCBO_CARO("NCBO-CARO"),
        NOVELGENE("NovelGene"),
        OMIM("OMIM"),
        PBLAST("PBLAST"),
        PREVEGA("PREVEGA"),
        PROSITE("PROSITE"),
        PFAM("Pfam"),
        PREENSEMBL_ZV7("PreEnsembl(Zv7)"),
        PUBMED("PubMed"),
        QUICKGO("QuickGO"),
        REFSEQ("RefSeq"),
        SGD("SGD"),
        SNPBLAST("SNPBLAST"),
        SP_KW("SP_KW"),
        SANGER_CLONE("Sanger_Clone"),
        TRACEVIEW("TraceView"),
        UNIGENE("UniGene"),
        UNIPROT("UniProt"),
        UNISTS("UniSTS"),
        VEGA("VEGA"),
        VEGA_CLONE("VEGA_Clone"),
        VEGA_TRANS("Vega_Trans"),
        WASHU("WashU"),
        WASHUZ("WashUZ"),
        ZF_ESPRESSO("ZF-Espresso")
            ;


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
