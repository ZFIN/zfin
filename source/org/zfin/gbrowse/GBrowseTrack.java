package org.zfin.gbrowse;

/**
 * Names of the gBrowse tracks.
 */
public enum GBrowseTrack {
    GENES("ZFIN Gene"),
    TRANSCRIPTS("Transcript"),
    CLONE("Assembly"),
    GENES_VEGA("genesVega"),
    ENSEMBL_MRNA("Ensembl_mRNA"),
    PHENOTYPE("ZFIN Genes with Phenotype"),
    EXPRESSIONS("ZFIN Genes with Expression"),
    ANTIBODY("ZFIN Genes with Antibody"),
    KNOCKDOWN_REAGENT("Knockdown Reagent"),
    INSERTION("Transgenic Insertion"),
    CNE("CNE"),
    COMPLETE_CLONES("Complete Assembly Clones"),
    ALLZMP("allzmp"),
    ZFIN_FEATURES("ZFIN Features"),
    ZMP("Zebrafish Mutation Project");

    GBrowseTrack(String trackName) {
        this.trackName = trackName;
    }

    private String trackName;

    public String toString() {
        return trackName;
    }

    public static GBrowseTrack fromString(String trackName) {
        for (GBrowseTrack gbrowseTrack : values()) {
            if (gbrowseTrack.trackName.equals(trackName)) {
                return gbrowseTrack;
            }
        }
        return null;
    }

}
