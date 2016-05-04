package org.zfin.gbrowse;

/**
 * Names of the gBrowse tracks.
 */
public enum GBrowseTrack {
    GENES("genes"),
    TRANSCRIPTS("mRNA"),
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
    ZMP("allzmp");

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
