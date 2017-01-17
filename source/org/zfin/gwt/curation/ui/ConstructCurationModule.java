package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.root.dto.AttributionType;
import org.zfin.gwt.root.dto.ConstructDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ShowHideToggle;

import java.util.ArrayList;
import java.util.List;


public class ConstructCurationModule extends ConstructionZoneAdapater implements ZfinCurationModule {


    public static final String CONSTRUCT_RELATIONSHIP_TEXT = "CONSTRUCT RELATIONSHIPS";
    public static final String CONSTRUCT_RELATIONSHIP = "construct-relationship";

    private HTML constructRelationshipLink = new HTML(CONSTRUCT_RELATIONSHIP_TEXT);
    public static final String CONSTRUCT_RELATIONSHIP_LINK = "construct-relationship-link";
    List<HandlesError> handlesErrorList = new ArrayList<>();
    private ShowHideToggle showHideToggle;
    private String publicationID;

    // modules
    private ConstructRelationshipBox constructRelationshipBox = new ConstructRelationshipBox();


    public ConstructCurationModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    public void init() {
        initGUI();
        addInternalListeners();
        loadDTO();
    }

    @Override
    public void refresh() {
        constructRelationshipBox.setValues();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {

    }

    @Override
    public void handleTabToggle() {

    }

    protected void addInternalListeners() {

        constructRelationshipBox.addHandlesErrorListener(this);
        showHideToggle.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                showHideToggle.toggleVisibility();
            }
        });
    }

    public void initGUI() {
        showHideToggle = new ShowHideToggle(constructRelationshipBox);
        HorizontalPanel titlePanel = new HorizontalPanel();
        constructRelationshipLink.addStyleName("bold");
        titlePanel.add(constructRelationshipLink);
        titlePanel.add(new HTML("&nbsp;"));
        titlePanel.add(showHideToggle);
        RootPanel.get(CONSTRUCT_RELATIONSHIP_LINK).add(titlePanel);
        RootPanel.get(CONSTRUCT_RELATIONSHIP).add(constructRelationshipBox);
    }

    private void loadDTO() {
        ConstructDTO featureDTO = new ConstructDTO();
        featureDTO.setPublicationZdbID(publicationID);
        constructRelationshipBox.setDTO(featureDTO);
    }


    public void clearError() {
        setError("");
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


    public void setError(String message) {
        if (!message.isEmpty()) {
            GWT.log("Error: " + message);
        }
    }

}
