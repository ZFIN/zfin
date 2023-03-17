package org.zfin.framework.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import org.zfin.framework.api.View;

import java.io.Serializable;

public class LookupEntry implements Serializable {

    @JsonView(View.API.class)
    protected String id;
    protected String name ;
    @JsonView(View.API.class)
    protected String label;
    @JsonView(View.API.class)
    protected String value;
    private String url;
    private String category;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
