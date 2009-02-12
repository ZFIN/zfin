package org.zfin.ontology;

/**
 * domain object for GO ontology. Will be superseeded by the gDAG.
 */
public class GoTerm implements OntologyTerm, Comparable<GoTerm> {

    private String zdbID;
    private String goID;
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

    public String getGoID() {
        return goID;
    }

    public void setGoID(String goID) {
        this.goID = goID;
    }

    public int compareTo(GoTerm goTerm) {
        if (goTerm == null)
            return 1;
        return name.compareTo(goTerm.getName());
    }
}
