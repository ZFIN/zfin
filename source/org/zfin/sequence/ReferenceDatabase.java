/**
 *  Class ReferenceDatabase.
 */
package org.zfin.sequence ;

import org.zfin.orthology.Species;

public class ReferenceDatabase {

    public enum SuperType{
        INFERENCE("inference"),
        ONTOLOGY("ontology"),
        ORTHOLOGUE("orthologue"),
        PROTEIN("protein"),
        PUBLICATION("publication"),
        SEQUENCE("sequence"),
        SUMMARY_PAGE("summary page");

        private String value;

        SuperType(String value) {
            this.value = value;
        }

        public String toString(){
            return this.value ; 
        }

    }

    public enum Type{
        OTHER("other"),
        GENOMIC("Genomic"),
        POLYPEPTIDE("Polypeptide"),
        SEQUENCE_CLUSTERS("Sequence Clusters"),
        VEGA_TRANSCRIPT("Vega Transcript"),
        CDNA("cDNA"),
        PUBLICATION("publication"),
        DOMAIN("domain"),
        ORTHOLOGUE("orthologue"),
        ANATOMY("anatomy"),
        CELL_ONTOLOGY("cell ontology"),
        COMMON_ANATOMY_REFERENCE_ONTOLOGY("common anatomy reference ontology"),
        GENE_ONTOLOGY("gene ontology");

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String toString(){
            return this.value ;
        }
    }

    private String zdbID;
    private ForeignDB foreignDB;
    private Species organism;
    private Type type;
    private SuperType superType;
    
    public ForeignDB getForeignDB() {
        return foreignDB;
    }

    public void setForeignDB(ForeignDB foreignDB) {
        this.foreignDB = foreignDB;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getBaseURL() {
        return foreignDB.getDbUrlPrefix();
    }

    public void setBaseURL(String baseURL) {
        foreignDB.setDbUrlPrefix(baseURL);
    }

    public Species getOrganism() {
        return organism;
    }

    public void setOrganism(Species organism) {
        this.organism = organism;
    }
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public SuperType getSuperType() {
        return superType;
    }

    public void setSuperType(SuperType superType) {
        this.superType = superType;
    }
}


