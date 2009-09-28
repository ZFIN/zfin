package org.zfin.sequence;

public class ForeignDBDataType {

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

        public static SuperType getType(String type) {
            for (SuperType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No reference supertype of string " + type + " found.");
        }
    }

    public enum DataType {
        ANATOMY("anatomy"),
        CELL_ONTOLOGY("cell ontology"),
        COMMON_ANATOMY_REFERENCE_ONTOLOGY("common anatomy reference ontology"),
        DOMAIN("domain"),
        GENE_ONTOLOGY("gene ontology"),
        GENOMIC("Genomic"),
        OTHER("other"),
        ORTHOLOGUE("orthologue"),
        POLYPEPTIDE("Polypeptide"),
        PUBLICATION("publication"),
        //VEGA_TRANSCRIPT("Vega Transcript"),
        RNA("RNA"),
        SEQUENCE_CLUSTERS("Sequence Clusters"),
        TELEOST_ANATOMY_ONTOLOGY("teleost anatomy ontology"),
        ;

        private String value;

        DataType(String value) {
            this.value = value;
        }

        public String toString(){
            return this.value ;
        }

        public static DataType getType(String type) {
            for (DataType t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No reference type of string " + type + " found.");
        }

    }

    private Long dataTypeID ;
    private DataType dataType;
    private SuperType superType;
    private int displayOrder;

    public Long getDataTypeID() {
        return dataTypeID;
    }

    public void setDataTypeID(Long dataTypeID) {
        this.dataTypeID = dataTypeID;
    }

    public SuperType getSuperType() {
        return superType;
    }

    public void setSuperType(SuperType superType) {
        this.superType = superType;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String toString(){
        String returnString = "" ;
        returnString += getClass().getName() + " " ;
        returnString += dataTypeID + " " ;
        returnString += dataType + " " ;
        returnString += superType + " " ;
        returnString += displayOrder + " " ;
        return returnString ;
    }

}
