package org.zfin.mapping;

import org.apache.commons.lang.StringUtils;

/**
 * Names of the gBrowse tracks.
 */
public enum GbrowseTrack {
    GENES("genes"),
    TRANSCRIPTS("mRNA"),
    CLONE("clone"),
    GENES_VEGA("genesVega"),
    ENSEMBL_MRNA("Ensembl_mRNA"),
    PHENOTYPE("phenotype"),
    EXPRESSIONS("expression"),
    ANTIBODY("antibody"),
    MORPHOLINO("morpholino"),
    INSERTION("insertion"),
    CNE("CNE"),;

    GbrowseTrack(String trackName) {
        this.trackName = trackName;
    }

    private String trackName;

    public static GbrowseTrack getGbrowseTrack(String name) {
        if (StringUtils.isEmpty(name))
            return null;
        for (GbrowseTrack track : values())
            if (track.getTrackName().equals(name))
                return track;
        return null;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}
