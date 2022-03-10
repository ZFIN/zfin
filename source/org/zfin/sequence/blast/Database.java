package org.zfin.sequence.blast;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Setter
@Getter
public class Database {

    @JsonView(View.SequenceAPI.class)
    private String zdbID;
    @JsonView(View.SequenceAPI.class)
    private String name;
    private AvailableAbbrev abbrev;
    @JsonView(View.SequenceAPI.class)
    private String description;
    @JsonView(View.SequenceAPI.class)
    private Type type;
    @JsonView(View.SequenceAPI.class)
    private String location;
    private boolean publicDatabase;
    private boolean isLocked;
    private Origination origination;
    @JsonView(View.SequenceAPI.class)
    private String displayName;
    private Integer toolDisplayOrder;

    // these will be ordered coming out of the database by order
    private Set<DatabaseRelationship> childrenRelationships;
    // these will be ordered coming out of the database by order
    private Set<DatabaseRelationship> parentRelationships;

    public String getShortType() {
        return type.toString().substring(0, 1);
    }

    public void setExpressionFound(boolean isPublic) {
        this.publicDatabase = isPublic;
    }

    public char getTypeCharacter() {
        return getType() == Database.Type.NUCLEOTIDE ? 'n' : 'p';
    }

    public File getTempFile(String prefix, String suffix) throws IOException {
        Path basedir = FileSystems.getDefault().getPath(ZfinPropertiesEnum.BLAST_ACCESSION_TEMP_DIR.value());
        Path tempPath = Files.createTempFile(basedir, prefix, suffix);
        File file = tempPath.toFile();
        boolean success = file.setReadable(true, false);
        return file;
    }

    public String getBackupWebHostDatabasePath() {
        return ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH + "/" + BlastService.BACKUP_DIRECTORY + "/" + getAbbrev().toString();
    }

    public String getBackupBlastServerDatabasePath() {
        return ZfinPropertiesEnum.BLASTSERVER_BLAST_DATABASE_PATH + "/" + BlastService.BACKUP_DIRECTORY + "/" + getAbbrev().toString();
    }

    public String getCurrentWebHostDatabasePath() {
        return ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH + "/" + BlastService.CURRENT_DIRECTORY + "/" + getAbbrev().toString();
    }

    public String getCurrentBlastServerDatabasePath() {
        return ZfinPropertiesEnum.BLASTSERVER_BLAST_DATABASE_PATH + "/" + BlastService.CURRENT_DIRECTORY + "/" + getAbbrev().toString();
    }

    public String getView() {
        return name + " " + abbrev.toString() + " " + type.toString();
    }

    @JsonView(View.SequenceAPI.class)
    public String getUrlPrefix() {
        if (location == null) {
            String prefix = "/action/blast/blast?&program=";
            prefix += type.equals(Type.NUCLEOTIDE) ? "blastn" : "blastp";
            prefix += "&sequenceType=";
            prefix += type.equals(Type.NUCLEOTIDE) ? "nt" : "pt";
            prefix += "&queryType=SEQUENCE_ID&dataLibraryString=";
            if (name.equals("ZFIN MicroRNA Sequences")) {
                prefix += abbrev.value;
            } else {
                prefix += "RNASequences";
            }
            prefix += "&sequenceID=";
            return prefix;
        } else {
            return location;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Database");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", abbrev=").append(abbrev);
        sb.append(", description='").append(description).append('\'');
        sb.append(", type=").append(type);
        sb.append(", location='").append(location).append('\'');
        sb.append(", publicDatabase=").append(publicDatabase);
        sb.append(", isLocked=").append(isLocked);
        sb.append(", displayName=").append(displayName);
        if (origination != null) {
            sb.append(", origination=").append(origination.getType().toString());
        }
        sb.append(", childrenRelationships=").append(childrenRelationships.size());
        sb.append(", parentRelationships=").append(parentRelationships.size());
        sb.append('}');
        return sb.toString();
    }

    public static enum AvailableAbbrev {

        BLAST("BLAST"),
        BLASTP("BLASTP"),
        BLAT("BLAT"),
        CURATEDMICRORNAMATURE("CuratedMicroRNAMature"),
        CURATEDMICRORNASTEMLOOP("CuratedMicroRNAStemLoop"),
        CURATEDNTRREGIONS("CuratedNtrRegions"),
        ENSEMBL("ENSEMBL"),
        ZFINENSEMBLTSCRIPT("zfinEnsemblTscript"),
        ENSEMBL_P("Ensembl_P"),
        ENSEMBL_ZF("ensembl_zf"),
        GBK_EST_HS("gbk_est_hs"),
        GBK_EST_MS("gbk_est_ms"),
        GBK_EST_ZF("gbk_est_zf"),
        GBK_GSS_ZF("gbk_gss_zf"),
        GBK_HS_DNA("gbk_hs_dna"),
        GBK_HS_MRNA("gbk_hs_mrna"),
        GBK_HTG_ZF("gbk_htg_zf"),
        GBK_MS_DNA("gbk_ms_dna"),
        GBK_MS_MRNA("gbk_ms_mrna"),
        GBK_ZF_DNA("gbk_zf_dna"),
        GBK_ZF_MRNA("gbk_zf_mrna"),
        GBK_ZF_RNA("gbk_zf_rna"),
        GENBANKZEBRAFISHRNA("GenBankZebrafishRNA"),
        GENOMICDNA("GenomicDNA"),
        LOADEDMICRORNAMATURE("LoadedMicroRNAMature"),
        LOADEDMICRORNASTEMLOOP("LoadedMicroRNAStemLoop"),
        LOADEDFISHMICRORNASTEMLOOP("LoadedFishMicroRNAStemLoop"),
        MEGA_BLAST("MEGA BLAST"),
        PBLAST("PBLAST"),
        PUBLISHEDPROTEIN("publishedProtein"),
        PUBLISHEDRNA("publishedRNA"),
        REFSEQ_ZF_AA("refseq_zf_aa"),
        REFSEQ_ZF_RNA("refseq_zf_rna"),
        REPBASE_ZF("repbase_zf"),
        RNASEQUENCES("RNASequences"),
        SNPBLAST("SNPBLAST"),
        SPTR_HS("sptr_hs"),
        SPTR_MS("sptr_ms"),
        SPTR_ZF("sptr_zf"),
        UCSC_BLAT("UCSC BLAT"),
        UNRELEASEDRNA("unreleasedRNA"),
        VEGA_TRANSCRIPT("vega_transcript"),
        VEGA("Vega"),
        VEGA_ZFIN("vega_zfin"),
        VEGAPROTEIN_ZF("vegaprotein_zf"),
        VEGA_WITHDRAWN("vega_withdrawn"),
        ZFIN_ALL_AA("zfin_all_aa"),
        ZFIN_CDNA_SEQ("zfin_cdna_seq"),
        ZFINGENESWITHEXPRESSION("ZFINGenesWithExpression"),
        ZFIN_MICRORNA("zfin_microRNA"),
        ZFIN_MIRNA_STEMLOOP("zfin_miRNA_stemloop"),
        ZFIN_MIRNA_MATURE("zfin_miRNA_mature"),
        ZFIN_MRPH("zfin_mrph"),
        ZFIN_TALEN("zfin_talen"),
        ZFIN_CRISPR("zfin_crispr"),
        ALL_REFPROT_AA("all_refprot_aa");


        private final String value;

        private AvailableAbbrev(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public String getValue() {
            return value;
        }

        public static AvailableAbbrev getType(String type) {
            for (AvailableAbbrev t : values()) {
                if (t.toString().equals(type.trim())) {
                    return t;
                }
            }
            throw new RuntimeException("No BlastDB named " + type + " found.");
        }

    }

    public static enum Type {
        NUCLEOTIDE("nucleotide"),
        PROTEIN("protein");

        private final String value;

        private Type(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static Type getType(String type) {
            for (Type t : values()) {
                if (t.toString().equals(type)) {
                    return t;
                }
            }
            throw new RuntimeException("No blast database of type " + type + " found.");
        }

        public boolean isNucleotide() {
            return value.equalsIgnoreCase(NUCLEOTIDE.toString());
        }

        public boolean isProtein() {
            return value.equalsIgnoreCase(PROTEIN.toString());
        }
    }
}

