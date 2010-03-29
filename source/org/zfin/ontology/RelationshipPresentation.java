package org.zfin.ontology;

import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class RelationshipPresentation {

    String type;
    List<Term> items;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Term> getItems() {
        return items;
    }

    public void setItems(List<Term> items) {
        this.items = items;
    }
}
