package org.zfin.marker.presentation;

import java.util.List;

/**
 * TODO: move more stuff off of MarkerBean
 */
public class GeneBean extends MarkerBean {

    boolean hasChimericClone ;
    private List<LinkDisplay> plasmidDBLinks;

    public boolean isHasChimericClone() {
        return hasChimericClone;
    }

    public void setHasChimericClone(boolean hasChimericClone) {
        this.hasChimericClone = hasChimericClone;
    }

    public List<LinkDisplay> getPlasmidDBLinks() {
        return plasmidDBLinks;
    }

    public void setPlasmidDBLinks(List<LinkDisplay> plasmidDBLinks) {
        this.plasmidDBLinks = plasmidDBLinks;
    }

}
