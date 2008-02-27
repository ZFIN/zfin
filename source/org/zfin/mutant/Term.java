package org.zfin.mutant;

/**
 * Pato ontology term.
 */
public class Term {

    public static final String QUALITY = "quality";

    public static final String TAG_NORMAL = "normal";
    public static final String TAG_ABNORMAL = "abnormal";

    private String zdbID;
    private String name;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
