package org.zfin.anatomy;

import org.zfin.infrastructure.DataAlias;

/**
 * This class holds the info about aliases for a specific anatomy item.
 */
public class AnatomySynonym extends DataAlias {

    private AnatomyItem item;

    public String getName() {
        return alias;
    }

    public void setName(String name) {
        alias = name;
    }

    public AnatomyItem getItem() {
        return item;
    }

    public void setItem(AnatomyItem item) {
        this.item = item;
    }

}
