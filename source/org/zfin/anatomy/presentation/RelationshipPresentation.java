package org.zfin.anatomy.presentation;

import org.zfin.anatomy.AnatomyItem;

import java.util.List;

/**
 * Please provide JavaDoc info!!!
 */
public class RelationshipPresentation {

    String type;
    List<AnatomyItem> items;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<AnatomyItem> getItems() {
        return items;
    }

    public void setItems(List<AnatomyItem> items) {
        this.items = items;
    }
}
