package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.marker.event.RelatedMarkerListener;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.dto.MarkerRelationshipEnumTypeGWTHack;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.event.RelatedEntityEvent;
import org.zfin.gwt.root.ui.AbstractRelatedEntityBox;

/**
 * A related marker box.
 */
public abstract class RelatedMarkerBox extends AbstractRelatedEntityBox<MarkerDTO> {


    MarkerRelationshipEnumTypeGWTHack type;
    private boolean zdbIDThenAbbrev;


    RelatedMarkerBox(MarkerRelationshipEnumTypeGWTHack type, boolean zdbIDThenAbbrev, String div) {
        super();
        this.type = type;
        this.zdbIDThenAbbrev = zdbIDThenAbbrev;
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }


    public void addRelatedEntity(final String name, final String pubZdbID) {
        // do client check
        String validationError = validateNewRelatedEntity(name);

        if (validationError != null) {
            Window.alert(validationError);
            return;
        }
        MarkerDTO dto = new MarkerDTO();
        dto.setZdbID(getZdbID());
        dto.setName(name);
        dto.setPublicationZdbID(pubZdbID);
        fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
    }

    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        MarkerDTO markerDTO = new MarkerDTO(relatedEntityDTO);
        fireAttributionRemoved(new RelatedEntityEvent<MarkerDTO>(markerDTO));
    }


    void addInternalListeners(final RelatedMarkerBox relatedMarkerbox) {
        addRelatedEntityCompositeListener(new RelatedMarkerListener(this));
    }


    public MarkerRelationshipEnumTypeGWTHack getType() {
        return type;
    }

    public void setType(MarkerRelationshipEnumTypeGWTHack type) {
        this.type = type;
    }

    public boolean isZdbIDThenAbbrev() {
        return zdbIDThenAbbrev;
    }

    public void setZdbIDThenAbbrev(boolean zdbIDThenAbbrev) {
        this.zdbIDThenAbbrev = zdbIDThenAbbrev;
    }

    public boolean isEditable(RelatedEntityDTO relatedEntityDTO) {
        return true;
    }
}
