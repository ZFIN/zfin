package org.zfin.marker;


/**
 *  Class MarkerRelationship.
 */
public class MarkerRelationship {

    public enum Type{

        CLONE_CONTAINS_GENE("clone contains gene"),
        CLONE_CONTAINS_SMALL_SEGMENT("clone contains small segment"),
        CLONE_OVERLAP("clone overlap"),
        CODING_SEQUENCE_OF("coding sequence of"),
        CONTAINS_OTHER_FEATURE("contains other feature"),
        CONTAINS_POLYMORPHISM("contains polymorphism"),
        GENE_CONTAINS_SMALL_SEGMENT("gene contains small segment"),
        GENE_ENCODES_SMALL_SEGMENT ("gene encodes small segment"),
        GENE_HYBRIDIZED_BY_SMALL_SEGMENT("gene hybridized by small segment"),
        KNOCKDOWN_REAGENT_TARGETS_GENE("knockdown reagent targets gene"),
        GENE_HAS_ARTIFACT("gene has artifact"),
        PROMOTER_OF("promoter of");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String toString(){
            return value;
        }

    }

    private String zdbID ;
    private Type type;
    private Marker firstMarker ;
    private Marker secondMarker ; 

    /**
     * Get zdbID.
     *
     * @return zdbID as String.
     */
    public String getZdbID()
    {
        return zdbID;
    }
    
    /**
     * Set zdbID.
     *
     * @param zdbID the value to set.
     */
    public void setZdbID(String zdbID)
    {
        this.zdbID = zdbID;
    }


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Get firstMarker.
     *
     * @return firstMarker as Marker.
     */
    public Marker getFirstMarker()
    {
        return firstMarker;
    }
    
    /**
     * Set firstMarker.
     *
     * @param firstMarker the value to set.
     */
    public void setFirstMarker(Marker firstMarker)
    {
        this.firstMarker = firstMarker;
    }
    
    /**
     * Get secondMarker.
     *
     * @return secondMarker as Marker.
     */
    public Marker getSecondMarker()
    {
        return secondMarker;
    }
    
    /**
     * Set secondMarker.
     *
     * @param secondMarker the value to set.
     */
    public void setSecondMarker(Marker secondMarker)
    {
        this.secondMarker = secondMarker;
    }

     public String toString(){
         String newline = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("MARKER_RELATIONSHIP");
        sb.append("zdbID: " + zdbID);
        sb.append(newline);
        sb.append("type: " + type);
         sb.append(newline);
        sb.append("firstMarker: " + firstMarker);
         sb.append(newline);
        sb.append("secondMarker: " + secondMarker);
         sb.append(newline);
        return sb.toString();

     }
}


