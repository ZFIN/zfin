package org.zfin.marker.presentation.event;

import org.zfin.marker.presentation.client.AbstractRelatedEntityBox;

/**
 */
public class RelatedEntityMarkerLoadedListener implements MarkerLoadListener {

    private AbstractRelatedEntityBox abstractRelatedEntityBox ;

    public RelatedEntityMarkerLoadedListener(AbstractRelatedEntityBox abstractRelatedEntityBox){
        this.abstractRelatedEntityBox = abstractRelatedEntityBox ;
    }

    public void markerDomainLoaded(MarkerLoadEvent markerLoadEvent) {
        abstractRelatedEntityBox.setRelatedEntities(markerLoadEvent.getMarkerDTO().getZdbID(),
                markerLoadEvent.getMarkerDTO().getRelatedGeneAttributes());
    }
}