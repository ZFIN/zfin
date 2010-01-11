package org.zfin.gwt.marker.event;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zfin.gwt.marker.ui.MarkerRPCService;
import org.zfin.gwt.marker.ui.RelatedMarkerBox;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.TermNotFoundException;

/**
 * Extends RelatedMarkerBoxListener so that it can give the option to create a clone
 * if none is found with the name provided.
 */
public class RelatedCloneListener extends RelatedMarkerListener {
    public RelatedCloneListener(RelatedMarkerBox relatedMarkerBox) {
        super(relatedMarkerBox);
    }

    public void addRelatedEntity(RelatedEntityEvent<MarkerDTO> relatedEntityEvent) {
        final MarkerDTO dto = relatedEntityEvent.getRelatedEntityDTO();
        dto.setZdbID(relatedMarkerBox.getZdbID());
        dto.setName(dto.getName());
        dto.setPublicationZdbID(dto.getPublicationZdbID());
        dto.setMarkerRelationshipType(relatedMarkerBox.getType().toString());
        dto.setZdbIDThenAbbrev(relatedMarkerBox.isZdbIDThenAbbrev());

        MarkerRPCService.App.getInstance().addRelatedMarker(dto, new AsyncCallback<MarkerDTO>() {
            public void onFailure(Throwable throwable) {
                if (throwable instanceof TermNotFoundException) {
                    //                            Window.alert("Failed to find clone: "+ attributionName);
                    boolean createClone = Window.confirm("Failed to find clone: " + dto.getName() + ".  Create in new window?");
                    if (createClone == true) {
                        Window.open("/action/marker/clone-add?name=" + dto.getName(), "_blank", "");
                    }


                } else {
                    relatedMarkerBox.setError("Exception adding clone: " + throwable);
                }
            }

            public void onSuccess(MarkerDTO markerDTO) {
                relatedMarkerBox.addRelatedEntityToGUI(markerDTO);
            }
        });
    }
}
