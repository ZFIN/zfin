package org.zfin.ontology;

/**
 * domain object for GO ontology. Will be superseeded by the gDAG.
 */
public class GoTerm {

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
