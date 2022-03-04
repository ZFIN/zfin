package org.zfin.gbrowse;

import org.zfin.genomebrowser.GenomeBrowserTrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Names of the gBrowse tracks.
 */
public enum GBrowseTrack {
    GENES("genes"),
    TRANSCRIPTS("transcript"),
    CLONE("clone"),
    GENES_VEGA("genesVega"),
    ENSEMBL_MRNA("Ensembl_mRNA"),
    PHENOTYPE("phenotype"),
    EXPRESSIONS("expression"),
    ANTIBODY("antibody"),
    KNOCKDOWN_REAGENT("knockdown_reagent"),
    INSERTION("insertion"),
    CNE("CNE"),
    COMPLETE_CLONES("fullclone"),
    ALLZMP("allzmp"),
    ZFIN_FEATURES("mutants"),
    ZMP("zmp");

    GBrowseTrack(String trackName) {
        this.trackName = trackName;
    }

    private String trackName;

    public String toString() {
        return trackName;
    }

    public static Collection<GBrowseTrack> fromGenomeBrowserTracks(Collection<GenomeBrowserTrack> tracks) {
        return tracks.stream().map(genomeBrowserTrack -> switch (genomeBrowserTrack) {
            case GENES -> GBrowseTrack.GENES;
            case TRANSCRIPTS -> GBrowseTrack.TRANSCRIPTS;
            case CLONE -> GBrowseTrack.CLONE;
            case GENES_VEGA -> GBrowseTrack.GENES_VEGA;
            case ENSEMBL_MRNA -> GBrowseTrack.ENSEMBL_MRNA;
            case PHENOTYPE -> GBrowseTrack.PHENOTYPE;
            case EXPRESSIONS -> GBrowseTrack.EXPRESSIONS;
            case ANTIBODY -> GBrowseTrack.ANTIBODY;
            case KNOCKDOWN_REAGENT -> GBrowseTrack.KNOCKDOWN_REAGENT;
            case INSERTION -> GBrowseTrack.INSERTION;
            case CNE -> GBrowseTrack.CNE;
            case COMPLETE_CLONES -> GBrowseTrack.COMPLETE_CLONES;
            case ALLZMP -> GBrowseTrack.ALLZMP;
            case ZFIN_FEATURES -> GBrowseTrack.ZFIN_FEATURES;
            case ZMP -> GBrowseTrack.ZMP;
        }).collect(Collectors.toList());
    }

}