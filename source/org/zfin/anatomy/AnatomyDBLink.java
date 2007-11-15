package org.zfin.anatomy;

import org.zfin.sequence.DBLink;

/**
 */
public class AnatomyDBLink extends DBLink {

    private AnatomyItem anatomyItem ;

    public AnatomyItem getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(AnatomyItem anatomyItem) {
        this.anatomyItem = anatomyItem;
    }
}
