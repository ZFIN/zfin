package org.zfin.marker.presentation;

import java.util.Collection;

public class MarkerAliasBean {

    private String alias;
    private Collection<String> references;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Collection<String> getReferences() {
        return references;
    }

    public void setReferences(Collection<String> references) {
        this.references = references;
    }
}
