package org.zfin.anatomy;

import org.zfin.ontology.GenericTerm;
import org.zfin.sequence.DBLink;

/**
 */
public class AnatomyDBLink extends DBLink {

    private GenericTerm anatomyItem ;

    public GenericTerm getAnatomyItem() {
        return anatomyItem;
    }

    public void setAnatomyItem(GenericTerm anatomyItem) {
        this.anatomyItem = anatomyItem;
    }
}
