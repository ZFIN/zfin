package org.zfin.genomebrowser;

import org.zfin.gbrowse.GBrowseTrack;

import java.util.*;
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
    private String trackId;

    public String toString() {
        return trackName;
    }

    public String getTrackId(String assembly) {
        //return trackName.replace(" ", "_") + "_" + assembly.toLowerCase();
        return trackMap.get(assembly).get(this);
    }

    static Map<String, Map<GenomeBrowserTrack, String>> trackMap = new HashMap<>();

    static {
        Map<GenomeBrowserTrack, String> trackIDMap12 = new HashMap<>();
        trackIDMap12.put(GENES, "zfin-gene12");
        trackIDMap12.put(REFSEQ, "refseq12");
        trackMap.put("GRCz12tu", trackIDMap12);

        Map<GenomeBrowserTrack, String> trackIDMap10 = new HashMap<>();
        trackIDMap10.put(GENES, "GRCz10_zfin_gene");
        trackMap.put("GRCz10", trackIDMap10);

        Map<GenomeBrowserTrack, String> trackIDMap11 = new HashMap<>();
        trackIDMap11.put(GENES, "zfin_gene");
        trackIDMap11.put(TRANSCRIPTS, "zfin_additional_transcripts");
        trackIDMap11.put(ZFIN_FEATURES, "zfin_features");
        trackIDMap11.put(ZFIN_MUTANT, "zfin_zebrafish_mutation_project");
        trackIDMap11.put(COMPLETE_CLONES, "zfin_complete_assembly_clones");
        trackMap.put("GRCz11", trackIDMap11);
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

    public enum Page {
        GENE_SEQUENCE(GENES, REFSEQ),
        GENE_TRANSCRIPTS(GENES, TRANSCRIPTS),
        MAPPING_DETAIL_GENES(GENES),
        CLONES(COMPLETE_CLONES, GENES, TRANSCRIPTS),
        GENE_STRS(GENES, KNOCKDOWN_REAGENT, TRANSCRIPTS),
        FEATURE(GENES, ZFIN_FEATURES, TRANSCRIPTS, ZFIN_MUTANT),
        ;
        private GenomeBrowserTrack[] tracks;

        Page(GenomeBrowserTrack... tracks) {
            this.tracks = tracks;
        }

        public GenomeBrowserTrack[] getTracks() {
            return tracks;
        }

    }

    public static GenomeBrowserTrack[] getGenomeBrowserTracks(Page page) {
        return Arrays.stream(Page.values()).filter(page1 -> page1 == page).findFirst().get().getTracks();
    }
}

