package org.zfin.gwt.curation.ui;

import com.google.gwt.user.client.Window;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class BaseCurationModule extends ConstructionZoneAdapater {

    // gui
    private AttributionModule attributionModule = new AttributionModule();

    // data
    private String publicationID;

    // listeners
    List<HandlesError> handlesErrorList = new ArrayList<HandlesError>();

    public BaseCurationModule(String publicationID) {
        this.publicationID = publicationID;
        initGUI();
        addInternalListeners(this);
    }

    private void addInternalListeners(HandlesError handlesError) {
        attributionModule.addHandlesErrorListener(handlesError);
    }

    private void initGUI() {

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);

        attributionModule.setDTO(relatedEntityDTO);
    }

    public ConstructionZone getPileConstructionZoneModule() {
        return this;
    }

    @Override
    public void setError(String message) {
    }

    @Override
    public void clearError() {
        // to propagate stuff
        String url = Window.Location.getPath() + Window.Location.getQueryString();
        Window.open(url, "_self", "");
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorList) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorList.add(handlesError);
    }
}
