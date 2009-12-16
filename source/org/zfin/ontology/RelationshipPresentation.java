package org.zfin.ontology;

import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class RelationshipPresentation {

    String type;
    List<GenericTerm> items;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<GenericTerm> getItems() {
        return items;
    }

    public void setItems(List<GenericTerm> items) {
        this.items = items;
    }
}
