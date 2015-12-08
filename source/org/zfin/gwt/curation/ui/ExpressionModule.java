package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for FX curation module.
 */
public class ExpressionModule implements HandlesError, EntryPoint {

    public static final String EXPRESSION_ZONE = "expressionZone";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExpressionModule> {
    }

    // data
    private String publicationID;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    AttributionModule attributionModule;
    @UiField
    ConstructionZoneModule constructionZoneModule;
    FxCurationModule fxCurationModule;

    private final HandlerManager eventBus = new HandlerManager(null);

    public ExpressionModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @Override
    public void onModuleLoad() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(EXPRESSION_ZONE).add(outer);

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        //constructionZoneModule.onModuleLoad();
        bindEventBusHandler();
        addHandlerEvents();
        
/*
        fxCurationModule = new FxCurationModule(publicationID);
        fxCurationModule.getStructureModule().setPileStructureClickListener(constructionZoneModule);
*/
    }

    public void addHandlerEvents() {
        attributionModule.addHandlesErrorListener(new HandlesError() {
            @Override
            public void setError(String message) {

            }

            @Override
            public void clearError() {

            }

            @Override
            public void fireEventSuccess() {
                Window.alert("Kui");
                eventBus.fireEvent(new RemoveAttributeEvent());
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {

            }
        });
    }

    private void bindEventBusHandler() {
/*
        eventBus.addHandler(AddNewDiseaseTermEvent.TYPE,
                new AddNewDiseaseTermHandler() {
                    @Override
                    public void onAddDiseaseTerm(AddNewDiseaseTermEvent event) {
                        diseaseModelPresenter.addDiseaseToSelectionBox(event.getDisease());
                    }
                });
        eventBus.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        diseaseModelPresenter.updateFishList();
                    }
                });
*/

    }

    @Override
    public void setError(String message) {

    }

    @Override
    public void clearError() {

    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }



}
