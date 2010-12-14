package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;


public class FeatureCurationModule extends ConstructionZoneAdapater {


    public static final String FEATURE_ADD = "add-feature";
    private final static String ADD_NEW_FEATURE_TEXT = "[Add New Feature]";
    public static final String FEATURE_EDIT = "edit-feature";
    public static final String FEATURE_RELATIONSHIP_TEXT = "[Feature Relationships]";
    public static final String FEATURE_RELATIONSHIP = "feature-relationship";
    private final static String EDIT_FEATURE_TEXT = "[Edit Feature]";
    private final static String RIGHT_ARROW_NEW_FEATURE = "<img align=\"top\" src=\"/images/right.gif\" >" + ADD_NEW_FEATURE_TEXT;
    private final static String DOWN_ARROW_NEW_FEATURE = "<img align=\"top\" src=\"/images/down.gif\" >" + ADD_NEW_FEATURE_TEXT;
    private final static String RIGHT_ARROW_EDIT_FEATURE = "<img align=\"top\" src=\"/images/right.gif\" >" + EDIT_FEATURE_TEXT;
    private final static String DOWN_ARROW_EDIT_FEATURE = "<img align=\"top\" src=\"/images/down.gif\" >" + EDIT_FEATURE_TEXT;
    private final static String RIGHT_ARROW_FEATURE_LINK  = "<img align=\"top\" src=\"/images/right.gif\" >" + FEATURE_RELATIONSHIP_TEXT;
    private final static String DOWN_ARROW_FEATURE_LINK  = "<img align=\"top\" src=\"/images/down.gif\" >" + FEATURE_RELATIONSHIP_TEXT;
    private HTML addNewFeatureLink = new HTML(RIGHT_ARROW_NEW_FEATURE);
    private HTML editFeatureLink = new HTML(RIGHT_ARROW_NEW_FEATURE);
    private HTML featureRelationshipLink = new HTML(RIGHT_ARROW_FEATURE_LINK);
    public static final String FEATURE_ADD_LINK = "feature-add-link";
    public static final String FEATURE_EDIT_LINK = "feature-edit-link";
    public static final String FEATURE_RELATIONSHIP_LINK = "feature-relationship-link";
    List<HandlesError> handlesErrorList = new ArrayList<HandlesError>();
    private String publicationID;

    // modules
    private AttributionModule attributionModule = new AttributionModule();
    private FeatureAddBox featureAddBox =new FeatureAddBox();
    private FeatureEditBox featureEditBox =new FeatureEditBox();
    private FeatureRelationshipBox featureRelationshipBox = new FeatureRelationshipBox();


    public FeatureCurationModule(String publicationID) {
        this.publicationID = publicationID;
        initGUI();
        addInternalListeners(this);
        loadDTO();

        openBoxNewFeature(false);
        openBoxEditFeature(false);
        openFeatureRelationshipsBox(false);
    }


    protected void addInternalListeners(HandlesError handlesError) {

        addNewFeatureLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openBoxNewFeature(!featureAddBox.isVisible());
            }

        });
        editFeatureLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openBoxEditFeature(!featureEditBox.isVisible());
            }

        });
        featureRelationshipLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openFeatureRelationshipsBox(!featureRelationshipBox.isVisible());
            }

        });
        attributionModule.addHandlesErrorListener(this);
        featureAddBox.addHandlesErrorListener(this);
        featureEditBox.addHandlesErrorListener(this);
        featureRelationshipBox.addHandlesErrorListener(this);
    }

    public void initGUI() {
        addNewFeatureLink.setStyleName("relatedEntityPubLink");
        RootPanel.get(FEATURE_ADD_LINK).add(addNewFeatureLink);
        RootPanel.get(FEATURE_ADD).add(featureAddBox);

        editFeatureLink.setStyleName("relatedEntityPubLink");
        RootPanel.get(FEATURE_EDIT_LINK).add(editFeatureLink);
        RootPanel.get(FEATURE_EDIT).add(featureEditBox);

        featureRelationshipLink.setStyleName("relatedEntityPubLink");
        RootPanel.get(FEATURE_RELATIONSHIP_LINK).add(featureRelationshipLink);
        RootPanel.get(FEATURE_RELATIONSHIP).add(featureRelationshipBox);
    }

    private void loadDTO() {
        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setPublicationZdbID(publicationID);
        featureAddBox.setDTO(featureDTO);
        attributionModule.setDTO(featureDTO);
        featureEditBox.setDTO(featureDTO);
        featureRelationshipBox.setDTO(featureDTO);
    }

    

    public void clearError() {
        setError("");
        attributionModule.revertGUI();
//        attributionModule.clearError();
        featureAddBox.setPublication(publicationID);
        featureAddBox.clearError();
        featureEditBox.setPublication(publicationID);
        featureEditBox.clearError();
        featureRelationshipBox.setPublication(publicationID);
        featureRelationshipBox.clearError();
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

    public void openBoxNewFeature(boolean b) {
        featureAddBox.setVisible(b);
        addNewFeatureLink.setHTML((b ? DOWN_ARROW_NEW_FEATURE : RIGHT_ARROW_NEW_FEATURE));
    }

    public void openBoxEditFeature(boolean b) {
        featureEditBox.setVisible(b);
        editFeatureLink.setHTML((b ? DOWN_ARROW_EDIT_FEATURE : RIGHT_ARROW_EDIT_FEATURE));
    }

    public void openFeatureRelationshipsBox(boolean b){
        featureRelationshipBox.setVisible(b);
        featureRelationshipLink.setHTML((b ? DOWN_ARROW_FEATURE_LINK : RIGHT_ARROW_FEATURE_LINK));
    }

    public void setError(String message) {
        if(!message.isEmpty()){
            Window.alert("Error: " + message);
        }
    }

}
