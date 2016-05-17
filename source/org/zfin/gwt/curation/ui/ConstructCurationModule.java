package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;


public class ConstructCurationModule extends ConstructionZoneAdapater {


    public static final String CONSTRUCT_RELATIONSHIP_TEXT = "[Construct Relationships]";
    public static final String CONSTRUCT_RELATIONSHIP = "construct-relationship";

    private final static String RIGHT_ARROW_FEATURE_LINK = "<img align=\"top\" src=\"/images/right.gif\" >" + CONSTRUCT_RELATIONSHIP_TEXT;
    private final static String DOWN_ARROW_FEATURE_LINK = "<img align=\"top\" src=\"/images/down.gif\" >" + CONSTRUCT_RELATIONSHIP_TEXT;

    private HTML constructRelationshipLink = new HTML(RIGHT_ARROW_FEATURE_LINK);
    public static final String CONSTRUCT_RELATIONSHIP_LINK = "construct-relationship-link";
    List<HandlesError> handlesErrorList = new ArrayList<HandlesError>();
    private String publicationID;

    // modules
    private AttributionModule attributionModule = new AttributionModule();
    private ConstructRelationshipBox constructRelationshipBox = new ConstructRelationshipBox();


    public ConstructCurationModule(String publicationID) {
        this.publicationID = publicationID;
        initGUI();
        addInternalListeners(this);
        loadDTO();


        openFeatureRelationshipsBox(false);
    }


    protected void addInternalListeners(HandlesError handlesError) {


        constructRelationshipLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openFeatureRelationshipsBox(!constructRelationshipBox.isVisible());
            }

        });
        attributionModule.addHandlesErrorListener(this);

        constructRelationshipBox.addHandlesErrorListener(this);
    }

    public void initGUI() {

        constructRelationshipLink.setStyleName("relatedEntityPubLink");
        RootPanel.get(CONSTRUCT_RELATIONSHIP_LINK).add(constructRelationshipLink);
        RootPanel.get(CONSTRUCT_RELATIONSHIP).add(constructRelationshipBox);
    }

    private void loadDTO() {
        ConstructDTO featureDTO = new ConstructDTO();
        featureDTO.setPublicationZdbID(publicationID);

        attributionModule.setDTO(featureDTO);

        constructRelationshipBox.setDTO(featureDTO);
    }


    public void clearError() {
        setError("");
        attributionModule.revertGUI();
//        attributionModule.clearError();
        //   constructRelationshipBox.setPublication(publicationID);
        constructRelationshipBox.clearError();
    }

    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorList) {
            handlesError.clearError();
        }
    }

    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorList.add(handlesError);
    }

    public ConstructionZone getPileConstructionZoneModule() {
        return this;
    }


    public void openFeatureRelationshipsBox(boolean b) {
        constructRelationshipBox.setVisible(b);
        constructRelationshipLink.setHTML((b ? DOWN_ARROW_FEATURE_LINK : RIGHT_ARROW_FEATURE_LINK));
    }

    public void setError(String message) {
        if (!message.isEmpty()) {
            GWT.log("Error: " + message);
        }
    }

}
