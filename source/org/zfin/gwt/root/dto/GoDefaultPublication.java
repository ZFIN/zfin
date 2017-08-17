package org.zfin.gwt.root.dto;

import java.util.HashSet;
import java.util.Set;

/**
 */
public enum GoDefaultPublication implements PubEnum {
    INTERPRO("InterPro2GO Mapping", "ZDB-PUB-020724-1", InferenceCategory.INTERPRO),
    UNIPROTKBKW("UNIPROTKB_KW2GO Mapping", "ZDB-PUB-020723-1", InferenceCategory.UNIPROTKB_KW),
    EC("EC2GO Mapping", "ZDB-PUB-031118-3", InferenceCategory.EC),
    ROOT("Annotation to Root Terms", "ZDB-PUB-031118-1"),
    ISS_REF_GENOME("ISS from Ref. Genome", "ZDB-PUB-071010-1"),
    ISS_MANUAL_CURATED("ISS from Manually Curated Orthology", "ZDB-PUB-040216-1"),
    GOREF_ENSEMBL("GO_REF:0000019", "ZDB-PUB-110127-1", InferenceCategory.ENSEMBL, false),
    GOREF_HAMAP("GO_REF:0000020", "ZDB-PUB-110127-2", InferenceCategory.HAMAP, false),
    GOREF_SP_SL("GO_REF:0000023", "ZDB-PUB-110127-3", InferenceCategory.SP_SL, false),
    GOREF_UNIPROTKB_SUBCELL1("GO_REF:0000039", "ZDB-PUB-120306-2", InferenceCategory.UNIPROTKB_SUBCELL, false),
    GOREF_UNIPROTKB_SUBCELL2("GO_REF:0000040", "ZDB-PUB-120306-4", InferenceCategory.UNIPROTKB_SUBCELL, false),
    GOREF_UNIPATHWAY("GO_REF:0000041","ZDB-PUB-130131-1",InferenceCategory.UNIPATHWAY,false),
    GOREF_UNIRULE("GO_REF:0000104","ZDB-PUB-170525-1",InferenceCategory.UNIRULE,false),
    GOREF_UNIPROT("GO_REF:0000024", "ZDB-PUB-110105-1", InferenceCategory.UNIPROTKB, false),
    GOREF_PAINT("GO_REF:0000033", "ZDB-PUB-110330-1", InferenceCategory.PANTHER, false),
    ;

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
            goRefPubs.add(GOREF_UNIPROTKB_SUBCELL1);
            goRefPubs.add(GOREF_UNIPROTKB_SUBCELL2);
            goRefPubs.add(GOREF_UNIPATHWAY);
            goRefPubs.add(GOREF_UNIRULE);
            goRefPubs.add(GOREF_HAMAP);
            goRefPubs.add(GOREF_ENSEMBL);
            goRefPubs.add(GOREF_UNIPROT);
            goRefPubs.add(GOREF_PAINT);
        }
        return goRefPubs;
    }

    public static Set<GoDefaultPublication> getIeaPubs() {
        if (ieaPubs == null) {
            ieaPubs = new HashSet<GoDefaultPublication>();
            ieaPubs.add(INTERPRO);
            ieaPubs.add(UNIPROTKBKW);
            ieaPubs.add(EC);
            ieaPubs.add(GOREF_SP_SL);
            ieaPubs.add(GOREF_UNIPROTKB_SUBCELL1);
            ieaPubs.add(GOREF_UNIPROTKB_SUBCELL2);
            ieaPubs.add(GOREF_UNIPATHWAY);
            goRefPubs.add(GOREF_UNIRULE);
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
