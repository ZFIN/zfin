package org.zfin.infrastructure;

import java.io.Serializable;


/**
 */
public class RecordAttribution implements Serializable {

    private String dataZdbID;
    private String sourceZdbID;
    private SourceType sourceType;

    public enum SourceType {
        ANATOMY_DEFINITION("anatomy definition"),
        CLONED_GENE("cloned gene"),
        FEATURE_TYPE("feature type"),
        MOLECULAR_CHARACTERIZATION("molecular characterization"),
        SEGREGATION("segregation"),
        STANDARD("standard"),
        FIRST_CURATED_SEQUENCE_PUB("first curated sequence pub");

        private final String value;

        SourceType(String value) {
            this.value = value;
        }

        public String toString() {
            return this.value;
        }
    }

    public String getDataZdbID() {
        return dataZdbID;
    }

    public void setDataZdbID(String dataZdbID) {
        this.dataZdbID = dataZdbID;
    }

    public String getSourceZdbID() {
        return sourceZdbID;
    }

    public void setSourceZdbID(String sourceZdbID) {
        this.sourceZdbID = sourceZdbID;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    //needs to override String methods for hash & equality

    public String getCompositeKey() {
        return dataZdbID + sourceZdbID;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RecordAttribution))
            return false;

        RecordAttribution recordAttribution = (RecordAttribution) o;
        return recordAttribution.getCompositeKey().equals(this.getCompositeKey());
    }

    public int hashCode() {
        return getCompositeKey().hashCode();
    }

    public String toString(){
        String returnString = "" ; 
        returnString += getDataZdbID()  ; 
        returnString += " "  ; 
        returnString += getSourceZdbID() ; 
        returnString += " "  ; 
        returnString += getSourceType() ; 

        return returnString ; 
    }

}
