package org.zfin.gwt.root.dto;

/**
 */
public enum GoCurationDefaultPublications implements PubEnum {
    INTERPRO("InterPro2GO Mapping", "ZDB-PUB-020724-1"),
    SPKW("SPKW2GO Mapping", "ZDB-PUB-020723-1"),
    EC("EC2GO Mapping", "ZDB-PUB-031118-3"),
    ROOT("Annotation to Root Terms", "ZDB-PUB-031118-1"),
    ISS_REF_GENOME("ISS from Ref. Genome", "ZDB-PUB-071010-1"),
    ISS_MANUAL_CURATED("ISS from Manually Curated Orthology", "ZDB-PUB-040216-1");

    private final String title;
    private final String zdbID;

    private GoCurationDefaultPublications(String title, String zdbID) {
        this.title = title;
        this.zdbID = zdbID;
    }

    public String title() {
        return title;
    }

    public String zdbID() {
        return zdbID;
    }


    public boolean equals(String zdbID) {
        if (zdbID != null && this.zdbID != null) {
            return zdbID.equals(this.zdbID);
        } else if (zdbID == null && this.zdbID == null) {
            return true;
        } else {
            return false;
        }
    }

    public static GoCurationDefaultPublications getPubForZdbID(String zdbID) {
        for (GoCurationDefaultPublications t : values()) {
            if (t.equals(zdbID.trim()))
                return t;
        }
        return null;
//        throw new RuntimeException("No Pub for zdbID " + zdbID + " found.");
    }

}
