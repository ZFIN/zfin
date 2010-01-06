package org.zfin.sequence.blast;

import org.zfin.properties.ZfinProperties;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 */
public class Database {

    private String zdbID ;
    private String name ;
    private AvailableAbbrev abbrev ;
    private String description ;
    private Type type ;
    private String location ;
    private boolean publicDatabase;
    private boolean isLocked ;
    private Origination origination ;
    private String displayName ;


    private Integer toolDisplayOrder;

    public Integer getToolDisplayOrder() {
        return toolDisplayOrder;
    }

    public void setToolDisplayOrder(Integer toolDisplayOrder) {
        this.toolDisplayOrder = toolDisplayOrder;
    }

    // unmapped, used only during database regeneration
    private transient File accessionFile ;


    // these will be ordered coming out of the database by order
    private Set<DatabaseRelationship> childrenRelationships ;
    // these will be ordered coming out of the database by order
    private Set<DatabaseRelationship> parentRelationships ;

    public Set<DatabaseRelationship> getChildrenRelationships() {
        return childrenRelationships;
    }

    public void setChildrenRelationships(Set<DatabaseRelationship> childrenRelationships) {
        this.childrenRelationships = childrenRelationships;
    }

    public Set<DatabaseRelationship> getParentRelationships() {
        return parentRelationships;
    }

    public void setParentRelationships(Set<DatabaseRelationship> parentRelationships) {
        this.parentRelationships = parentRelationships;
    }

    public Origination getOrigination() {
        return origination;
    }

    public void setOrigination(Origination origination) {
        this.origination = origination;
    }


    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getShortType(){
        return type.toString().substring(0,1) ;
    }

    public void setExpressionFound(boolean isPublic) {
        this.publicDatabase = isPublic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AvailableAbbrev getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(AvailableAbbrev abbrev) {
        this.abbrev = abbrev;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Type getType() {
        return type;
    }

    public char getTypeCharacter() {
        return getType()==Database.Type.NUCLEOTIDE ? 'n' : 'p' ;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public boolean isPublicDatabase() {
        return publicDatabase;
    }

    public void setPublicDatabase(boolean publicDatabase) {
        this.publicDatabase = publicDatabase;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public File getAccessionFile() throws IOException {
        if(accessionFile==null){
            accessionFile = File.createTempFile("accessions",".txt") ;
        }
        return accessionFile;
    }

    public void setAccessionFile(File accessionFile) {
        this.accessionFile = accessionFile;
    }

    public String getBackupWebHostDatabasePath(){
        return ZfinProperties.getWebHostDatabasePath()  + "/" + BlastService.BACKUP_DIRECTORY + "/" + getAbbrev().toString() ;
    }

    public String getBackupBlastServerDatabasePath(){
        return ZfinProperties.getBlastServerDatabasePath() + "/" + BlastService.BACKUP_DIRECTORY + "/" + getAbbrev().toString()  ;
    }

    public String getCurrentWebHostDatabasePath(){
        return ZfinProperties.getWebHostDatabasePath()  + "/" + BlastService.CURRENT_DIRECTORY + "/" + getAbbrev().toString() ;
    }

    public String getCurrentBlastServerDatabasePath(){
        return ZfinProperties.getBlastServerDatabasePath() + "/" + BlastService.CURRENT_DIRECTORY + "/" + getAbbrev().toString()  ;
    }

    public String getView(){
        return name + " " + abbrev.toString() + " " + type.toString() ;
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
        sb.append(", origination=").append(origination.getType().toString());
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
        ENSEMBL("ENSEMBL"),
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
        TIGR_ZF("tigr_zf"),
        UCSC_BLAT("UCSC BLAT"),
        UNRELEASEDPROTEIN("unreleasedProtein"),
        UNRELEASEDRNA("unreleasedRNA"),
        VEGA_TRANSCRIPT("vega_transcript"),
        VEGA("Vega"),
        VEGA_ZFIN("vega_zfin"),
        VEGA_BLAST("VEGA_BLAST"),
	VEGA_WITHDRAWN("vega_withdrawn"),
        WGS_ZF("wgs_zf"),
        ZFIN_ALL_AA("zfin_all_aa"),
        ZFIN_CDNA_SEQ("zfin_cdna_seq"),
        ZFINGENESWITHEXPRESSION("ZFINGenesWithExpression"),
        ZFIN_MICRORNA("zfin_microRNA"),
        ZFIN_MIRNA_STEMLOOP("zfin_miRNA_stemloop"),
        ZFIN_MIRNA_MATURE("zfin_miRNA_mature"),
        ZFIN_MRPH("zfin_mrph"),
        ;


        private final String value;

        private AvailableAbbrev(String type) {
            this.value = type;
        }

        public String toString() {
            return this.value;
        }

        public static AvailableAbbrev getType(String type) {
            for (AvailableAbbrev t : values()) {
                if (t.toString().equals(type.trim()))
                    return t;
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
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No blast database of type " + type + " found.");
        }

        public boolean isNucleotide(){
            return value.equalsIgnoreCase(NUCLEOTIDE.toString()) ;
        }

        public boolean isProtein(){
            return value.equalsIgnoreCase(PROTEIN.toString()) ;
        }
    }
}

