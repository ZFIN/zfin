package org.zfin.gwt.marker.event;

import org.zfin.gwt.marker.ui.RelatedMarkerBox;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.event.RelatedEntityListener;
import org.zfin.gwt.root.ui.MarkerEditCallBack;
import org.zfin.gwt.root.ui.MarkerRPCService;

public class RelatedMarkerListener implements RelatedEntityListener<MarkerDTO> {

    RelatedMarkerBox relatedMarkerBox;

    public RelatedMarkerListener(RelatedMarkerBox relatedMarkerBox) {
        this.relatedMarkerBox = relatedMarkerBox;
    }


    public void addRelatedEntity(RelatedEntityEvent<MarkerDTO> relatedEntityEvent) {

        final MarkerDTO dto = relatedEntityEvent.getDTO();
        dto.setZdbID(relatedMarkerBox.getZdbID());
        dto.setName(dto.getName());
        dto.setPublicationZdbID(dto.getPublicationZdbID());
        dto.setMarkerRelationshipType(relatedMarkerBox.getType().toString());
        dto.setZdbIDThenAbbrev(relatedMarkerBox.isZdbIDThenAbbrev());


        MarkerRPCService.App.getInstance().addRelatedMarker(dto,
                new MarkerEditCallBack<MarkerDTO>("Exception adding entity: ", relatedMarkerBox) {
                    public void onSuccess(MarkerDTO markerDTO) {
                        relatedMarkerBox.addRelatedEntityToGUI(markerDTO);
                    }
                });

    }

    public void addAttribution(RelatedEntityEvent<MarkerDTO> relatedEntityEvent) {
        final MarkerDTO dto = relatedEntityEvent.getDTO();
        dto.setZdbID(relatedMarkerBox.getZdbID());
        dto.setName(dto.getName());
        dto.setPublicationZdbID(dto.getPublicationZdbID());
        dto.setMarkerRelationshipType(relatedMarkerBox.getType().toString());
        dto.setZdbIDThenAbbrev(relatedMarkerBox.isZdbIDThenAbbrev());

        MarkerRPCService.App.getInstance().addRelatedMarkerAttribution(dto,
                new MarkerEditCallBack<MarkerDTO>("failed to add attribution: ", relatedMarkerBox) {
                    public void onSuccess(MarkerDTO markerDTO) {
                        relatedMarkerBox.addAttributionToGUI(markerDTO);
                    }
                });
    }

    public void removeRelatedEntity(RelatedEntityEvent<MarkerDTO> relatedEntityEvent) {
        final MarkerDTO dto = relatedEntityEvent.getDTO();
        dto.setZdbID(relatedMarkerBox.getZdbID());
        dto.setName(dto.getName());
        dto.setPublicationZdbID(dto.getPublicationZdbID());
        dto.setMarkerRelationshipType(relatedMarkerBox.getType().toString());
        dto.setZdbIDThenAbbrev(relatedMarkerBox.isZdbIDThenAbbrev());

        MarkerRPCService.App.getInstance().removeRelatedMarker(dto,
                new MarkerEditCallBack<Void>("failed to remove entity: ", relatedMarkerBox) {
                    public void onSuccess(Void o) {
                        relatedMarkerBox.removeRelatedEntityFromGUI(dto);
                    }
                });

    }

    public void removeAttribution(RelatedEntityEvent<MarkerDTO> relatedEntityEvent) {
        final MarkerDTO dto = relatedEntityEvent.getDTO();
        dto.setZdbID(relatedMarkerBox.getZdbID());
        dto.setName(dto.getName());
        dto.setPublicationZdbID(dto.getPublicationZdbID());
        dto.setMarkerRelationshipType(relatedMarkerBox.getType().toString());
        dto.setZdbIDThenAbbrev(relatedMarkerBox.isZdbIDThenAbbrev());

        MarkerRPCService.App.getInstance().removeRelatedMarkerAttribution(dto,
                new MarkerEditCallBack<Void>("failed to remove attribution: ", relatedMarkerBox) {
                    public void onSuccess(Void o) {
                        relatedMarkerBox.removeAttributionFromGUI(dto);
                    }
                });
    }
}




