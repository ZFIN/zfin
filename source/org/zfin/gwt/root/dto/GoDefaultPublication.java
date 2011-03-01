package org.zfin.gwt.root.dto;

import java.util.HashSet;
import java.util.Set;

/**
 */
public enum GoDefaultPublication implements PubEnum {
    INTERPRO("InterPro2GO Mapping", "ZDB-PUB-020724-1", InferenceCategory.INTERPRO),
    SPKW("SPKW2GO Mapping", "ZDB-PUB-020723-1", InferenceCategory.SP_KW),
    EC("EC2GO Mapping", "ZDB-PUB-031118-3", InferenceCategory.EC),
    ROOT("Annotation to Root Terms", "ZDB-PUB-031118-1"),
    ISS_REF_GENOME("ISS from Ref. Genome", "ZDB-PUB-071010-1"),
    ISS_MANUAL_CURATED("ISS from Manually Curated Orthology", "ZDB-PUB-040216-1"),
    GOREF_ENSEMBL("GO_REF:0000019", "ZDB-PUB-110127-1", InferenceCategory.ENSEMBL, false),
    GOREF_HAMAP("GO_REF:0000020", "ZDB-PUB-110127-2", InferenceCategory.HAMAP, false),
    GOREF_SP_SL("GO_REF:0000023", "ZDB-PUB-110127-3", InferenceCategory.SP_SL, false),
    GOREF_UNIPROT("GO_REF:0000024", "ZDB-PUB-110105-1", InferenceCategory.UNIPROTKB, false),;

    private final String title;
    private final String zdbID;
    private final InferenceCategory inferenceCategory;
    private final boolean curated;

    private static Set<GoDefaultPublication> goRefPubs = null;
    private static Set<GoDefaultPublication> ieaPubs = null;

    private GoDefaultPublication(String title, String zdbID) {
        this(title, zdbID, null, true);
    }

    private GoDefaultPublication(String title, String zdbID, InferenceCategory inferenceCategory) {
        this(title, zdbID, inferenceCategory, true);
    }

    private GoDefaultPublication(String title, String zdbID, InferenceCategory inferenceCategory, boolean curated) {
        this.title = title;
        this.zdbID = zdbID;
        this.inferenceCategory = inferenceCategory;
        this.curated = curated;
    }

    public String title() {
        return title;
    }

    public String zdbID() {
        return zdbID;
    }

    public InferenceCategory inferenceCategory() {
        return inferenceCategory;
    }

    public static GoDefaultPublication[] getCurationPublications() {
        Set<GoDefaultPublication> goCuratedPublications = new HashSet<GoDefaultPublication>();
        for (GoDefaultPublication goDefaultPublication : values()) {
            if (goDefaultPublication.curated()) {
                goCuratedPublications.add(goDefaultPublication);
            }
        }
        return goCuratedPublications.toArray(new GoDefaultPublication[goCuratedPublications.size()]);
    }

    public boolean curated() {
        return curated;
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

    public static GoDefaultPublication getPubForZdbID(String zdbID) {
        for (GoDefaultPublication t : values()) {
            if (t.equals(zdbID.trim()))
                return t;
        }
        return null;
//        throw new RuntimeException("No Pub for zdbID " + zdbID + " found.");
    }

    public static Set<GoDefaultPublication> getGoRefPubs() {
        if (goRefPubs == null) {
            goRefPubs = new HashSet<GoDefaultPublication>();
            goRefPubs.add(GOREF_SP_SL);
            goRefPubs.add(GOREF_HAMAP);
            goRefPubs.add(GOREF_ENSEMBL);
            goRefPubs.add(GOREF_UNIPROT);
        }
        return goRefPubs;
    }

    public static Set<GoDefaultPublication> getIeaPubs() {
        if (ieaPubs == null) {
            ieaPubs = new HashSet<GoDefaultPublication>();
            ieaPubs.add(INTERPRO);
            ieaPubs.add(SPKW);
            ieaPubs.add(EC);
            ieaPubs.add(GOREF_SP_SL);
            ieaPubs.add(GOREF_HAMAP);
            ieaPubs.add(GOREF_ENSEMBL);
        }
        return ieaPubs;
    }

    public static String validateInference(String publicationZdbID, InferenceCategory inferenceCategory) {
        GoDefaultPublication goDefaultPublication = getPubForZdbID(publicationZdbID);
        if (goDefaultPublication != null
                && goDefaultPublication.inferenceCategory() != inferenceCategory) {
            return "Pub [" + publicationZdbID + "] must have inference [" + goDefaultPublication.inferenceCategory().toString() + "]";
        }
        return null;
    }

    public static boolean isIeaPublication(String publicationZdbID) {
        return getIeaPubs().contains(getPubForZdbID(publicationZdbID));
    }
}
