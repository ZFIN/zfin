package org.zfin.marker.presentation;

import java.io.Serializable;

/**
 */
public class AntibodyLookupEntry implements Serializable, Comparable<AntibodyLookupEntry> {
    private String id;
    private String label;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int compareTo(AntibodyLookupEntry anotherAntibodyEntry) {
        return label.compareTo(anotherAntibodyEntry.getLabel());
    }
}
