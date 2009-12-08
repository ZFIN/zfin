package org.zfin.ontology;

/**
 * domain object for GO ontology. Will be superseeded by the gDAG.
 */
public class GoTerm implements OntologyTerm, Comparable<GoTerm> {

    private String zdbID;
    private String goID;
    private String name;
    private String subOntology;
    private boolean obsolete;

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

    public String getId() {
        return zdbID;
    }

    public String getTermName() {
        return name;
    }

    public String getOboID(){
        if(goID == null)
            return null;

        return "GO:"+goID;
    }

    public String getSubOntology() {
        return subOntology;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public void setSubOntology(String subOntology) {
        this.subOntology = subOntology;
    }

    public int compareTo(GoTerm goTerm) {
        if (goTerm == null)
            return 1;
        return name.compareTo(goTerm.getName());
    }
}
