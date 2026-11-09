package org.zfin.publication;

/**
 * How a {@link PublicationFile} got into ZFIN. A null source on a row means unknown
 * (rows created before this was recorded).
 */
public enum PublicationFileSource {
    /** Uploaded by a curator on the publication page. */
    CURATOR_UPLOAD("Curator upload"),
    /** Fetched by the PMC Open Access loader (getPDFandImages.groovy). */
    PMC_LOADER("PMC loader"),
    /** Attached to a ZebraShare submission. */
    ZEBRASHARE_SUBMISSION("ZebraShare submission");

    private final String label;

    PublicationFileSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
