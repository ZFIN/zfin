package org.zfin.gwt.marker.ui;

import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;


public class ImageTermListener implements RelatedEntityListener {

    private ImageAnatomyBox imageAnatomyBox;

    public ImageTermListener(ImageAnatomyBox box) {
        imageAnatomyBox = box;
    }

    public void addRelatedEntity(RelatedEntityEvent event) {
        //todo: go to the backend, rather than just adding to the gui!
//        imageAnatomyBox.addRelatedEntityToGUI(event.getDTO());
    }

    public void addAttribution(RelatedEntityEvent relatedEntityEvent) {
        //no attribution to handle
     }

    public void removeRelatedEntity(RelatedEntityEvent event) {
//        imageAnatomyBox.removeAttributionFromGUI(event.getDTO());
    }

    public void removeAttribution(RelatedEntityEvent relatedEntityEvent) {
        //no attribution to handle
    }
}
