package org.zfin.genomebrowser;

import org.zfin.gbrowse.GBrowseTrack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Names of the gBrowse tracks.
 */
public enum GenomeBrowserTrack {
    GENES("ZFIN Gene"),
    TRANSCRIPTS("Transcript"),
    ADDITIONAL_TRANSCRIPTS("Additional Transcripts"),
    CLONE("Assembly"),
    GENES_VEGA("genesVega"),
    ENSEMBL_MRNA("Ensembl_mRNA"),
    PHENOTYPE("ZFIN Genes with Phenotype"),
    EXPRESSIONS("ZFIN Genes with Expression"),
    ANTIBODY("ZFIN Genes with Antibody"),
    KNOCKDOWN_REAGENT("Knockdown Reagent"),
    INSERTION("Transgenic Insertion"),
    ZFIN_MUTANT("ZFIN Mutants"),
    CNE("CNE"),
    COMPLETE_CLONES("Complete Assembly Clones"),
    ALLZMP("allzmp"),
    ZFIN_FEATURES("ZFIN Features"),
    ZMP("Zebrafish Mutation Project"),
    REFSEQ("RefSeq");

    GenomeBrowserTrack(String trackName) {
        this.trackName = trackName;
    }

    private String trackName;

    public String toString() {
        return trackName;
    }

    public static GenomeBrowserTrack fromString(String trackName) {
        for (GenomeBrowserTrack gbrowseTrack : values()) {
            if (gbrowseTrack.trackName.equals(trackName)) {
                return gbrowseTrack;
            }
        }
        return null;
    }

    public static Collection<GBrowseTrack> convertGenomeBrowserTracksToGBrowse(Collection<GenomeBrowserTrack> tracks) {
        if (tracks == null) {
            return new ArrayList<>();
        }
        return tracks
                .stream()
                .filter(track -> track != null)
                .map(GenomeBrowserTrack::convertGenomeBrowserTrackToGBrowse)
                .collect(Collectors.toList());
    }

    public static GBrowseTrack convertGenomeBrowserTrackToGBrowse(GenomeBrowserTrack genomeBrowserTrack) {
        return switch (genomeBrowserTrack) {
            case GENES -> GBrowseTrack.GENES;
            case TRANSCRIPTS -> GBrowseTrack.TRANSCRIPTS;
            case ADDITIONAL_TRANSCRIPTS -> GBrowseTrack.ADDITIONAL_TRANSCRIPTS;
            case CLONE -> GBrowseTrack.CLONE;
            case GENES_VEGA -> GBrowseTrack.GENES_VEGA;
            case ENSEMBL_MRNA -> GBrowseTrack.ENSEMBL_MRNA;
            case PHENOTYPE -> GBrowseTrack.PHENOTYPE;
            case EXPRESSIONS -> GBrowseTrack.EXPRESSIONS;
            case ANTIBODY -> GBrowseTrack.ANTIBODY;
            case KNOCKDOWN_REAGENT -> GBrowseTrack.KNOCKDOWN_REAGENT;
            case INSERTION -> GBrowseTrack.INSERTION;
            case ZFIN_MUTANT -> GBrowseTrack.ZFIN_MUTANT;
            case CNE -> GBrowseTrack.CNE;
            case COMPLETE_CLONES -> GBrowseTrack.COMPLETE_CLONES;
            case ALLZMP -> GBrowseTrack.ALLZMP;
            case ZFIN_FEATURES -> GBrowseTrack.ZFIN_FEATURES;
            case ZMP -> GBrowseTrack.ZMP;
            case REFSEQ -> GBrowseTrack.REF_SEQ;
        };
    }
}
