package org.zfin.gwt.marker.ui;

import org.zfin.gwt.marker.event.RelatedCloneListener;

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