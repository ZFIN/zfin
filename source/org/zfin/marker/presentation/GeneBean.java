package org.zfin.marker.presentation;

import java.util.List;

/**
 * TODO: move more stuff off of MarkerBean
 */

public class GeneBean extends MarkerBean {

    boolean hasChimericClone ;
    private List<LinkDisplay> plasmidDBLinks;
    private List<LinkDisplay> pathwayDBLinks;

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

    public List<LinkDisplay> getPathwayDBLinks() {
        return pathwayDBLinks;
    }

    public void setPathwayDBLinks(List<LinkDisplay> pathwayDBLinks) {
        this.pathwayDBLinks = pathwayDBLinks;
    }
}
