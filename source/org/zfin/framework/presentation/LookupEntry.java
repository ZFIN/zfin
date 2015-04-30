package org.zfin.framework.presentation;

import java.io.Serializable;

public class LookupEntry implements Serializable {

    protected String id;
    protected String name ;
    protected String label;
    protected String value;

    public LookupEntry() {  }

    public LookupEntry(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
