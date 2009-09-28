package org.zfin.marker.presentation.client;

import org.zfin.marker.presentation.event.RelatedCloneListener;

/**
 */
public class RelatedCloneBox extends RelatedMarkerBox {

    public RelatedCloneBox(MarkerRelationshipEnumTypeGWTHack type,boolean zdbIDThenAbbrev,String div){
        super(type,zdbIDThenAbbrev,div) ;
    }

    @Override
    public void addInternalListeners(final RelatedMarkerBox relatedMarkerbox){
        addRelatedEntityCompositeListener(new RelatedCloneListener(this));
    }

}