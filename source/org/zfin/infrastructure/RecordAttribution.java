package org.zfin.infrastructure;

import java.io.Serializable;


/**
 */
public class RecordAttribution implements Serializable {

    private String dataZdbID;
    private String sourceZdbID;
    private String sourceType;

    public enum SourceType {
        ANATOMY_DEFINITION("anatomy definition"),
        CLONED_GENE("cloned gene"),
        FEATURE_TYPE("feature type"),
        MOLECULAR_CHARACTERIZATION("molecular characterization"),
        SEGREGATION("segregation"),
        STANDARD("standard");

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

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
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
}
