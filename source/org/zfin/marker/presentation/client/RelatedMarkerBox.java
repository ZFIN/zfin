package org.zfin.marker.presentation.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import org.zfin.marker.presentation.dto.RelatedEntityDTO;
import org.zfin.marker.presentation.dto.MarkerDTO;
import org.zfin.marker.presentation.event.RelatedEntityEvent;
import org.zfin.marker.presentation.event.RelatedMarkerListener;

/**
 */
public abstract class RelatedMarkerBox extends AbstractRelatedEntityBox<MarkerDTO> {



    protected MarkerRelationshipEnumTypeGWTHack type ;
    protected boolean zdbIDThenAbbrev ;


    public RelatedMarkerBox(MarkerRelationshipEnumTypeGWTHack type, boolean zdbIDThenAbbrev,String div){
        super() ;
        this.type = type ;
        this.zdbIDThenAbbrev = zdbIDThenAbbrev ;
        addInternalListeners(this);
        RootPanel.get(div).add(this);
    }

    
    public void addRelatedEntity(final String name,final String pubZdbID) {
        // do client check
        String validationError = validateNewRelatedEntity(name);

        if(validationError!=null){
            Window.alert(validationError);
            return ;
        }
        MarkerDTO dto = new MarkerDTO() ;
        dto.setZdbID(getZdbID()) ;
        dto.setName(name);
        dto.setPublicationZdbID(pubZdbID);
        fireRelatedEntityAdded(new RelatedEntityEvent<MarkerDTO>(dto));
    }

    public void removeAttribution(RelatedEntityDTO relatedEntityDTO) {
        MarkerDTO markerDTO= new MarkerDTO(relatedEntityDTO) ;
        fireAttributionRemoved(new RelatedEntityEvent<MarkerDTO>(markerDTO));
    }


   public void addInternalListeners(final RelatedMarkerBox relatedMarkerbox){
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
        return true ;
    }
}