package org.zfin.gbrowse;

import org.zfin.genomebrowser.GenomeBrowserTrack;

/**
 * Names of the gBrowse tracks.
 */
public enum GBrowseTrack {
    GENES("genes"),
    TRANSCRIPTS("transcript"),
    ADDITIONAL_TRANSCRIPTS("Additional transcripts"),
    CLONE("clone"),
    GENES_VEGA("genesVega"),
    ENSEMBL_MRNA("Ensembl_mRNA"),
    PHENOTYPE("phenotype"),
    EXPRESSIONS("expression"),
    ANTIBODY("antibody"),
    KNOCKDOWN_REAGENT("knockdown_reagent"),
    INSERTION("insertion"),
    ZFIN_MUTANT("mutant"),
    CNE("CNE"),
    COMPLETE_CLONES("fullclone"),
    ALLZMP("allzmp"),
    ZFIN_FEATURES("mutants"),
    REF_SEQ("Full RefSeq GFF"),
    ZMP("zmp");

    GBrowseTrack(String trackName) {
        this.trackName = trackName;
    }

    private String trackName;

    public String toString() {
        return trackName;
    }

    public static GenomeBrowserTrack convertGBrowseTrackToGenomeBrowserTrack(GBrowseTrack genomeBrowserTrack) {
        return switch (genomeBrowserTrack) {
            case GENES -> GenomeBrowserTrack.GENES;
            case TRANSCRIPTS -> GenomeBrowserTrack.TRANSCRIPTS;
            case ADDITIONAL_TRANSCRIPTS -> GenomeBrowserTrack.ADDITIONAL_TRANSCRIPTS;
            case CLONE -> GenomeBrowserTrack.CLONE;
            case GENES_VEGA -> GenomeBrowserTrack.GENES_VEGA;
            case ENSEMBL_MRNA -> GenomeBrowserTrack.ENSEMBL_MRNA;
            case PHENOTYPE -> GenomeBrowserTrack.PHENOTYPE;
            case EXPRESSIONS -> GenomeBrowserTrack.EXPRESSIONS;
            case ANTIBODY -> GenomeBrowserTrack.ANTIBODY;
            case KNOCKDOWN_REAGENT -> GenomeBrowserTrack.KNOCKDOWN_REAGENT;
            case INSERTION -> GenomeBrowserTrack.INSERTION;
            case ZFIN_MUTANT -> GenomeBrowserTrack.ZFIN_MUTANT;
            case CNE -> GenomeBrowserTrack.CNE;
            case COMPLETE_CLONES -> GenomeBrowserTrack.COMPLETE_CLONES;
            case ALLZMP -> GenomeBrowserTrack.ALLZMP;
            case ZFIN_FEATURES -> GenomeBrowserTrack.ZFIN_FEATURES;
            case ZMP -> GenomeBrowserTrack.ZMP;
            case REF_SEQ -> GenomeBrowserTrack.REFSEQ;
        };
    }    
    
}