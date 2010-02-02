package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.AbstractRelatedEntityBox;

/**
 */
public class RelatedEntityMarkerLoadedListener implements MarkerLoadListener {

    private AbstractRelatedEntityBox abstractRelatedEntityBox ;

    public RelatedEntityMarkerLoadedListener(AbstractRelatedEntityBox abstractRelatedEntityBox){
        this.abstractRelatedEntityBox = abstractRelatedEntityBox ;
    }

    public void markerLoaded(MarkerLoadEvent markerLoadEvent) {
        abstractRelatedEntityBox.setRelatedEntities(markerLoadEvent.getMarkerDTO().getZdbID(),
                markerLoadEvent.getMarkerDTO().getRelatedGeneAttributes());
    }
}