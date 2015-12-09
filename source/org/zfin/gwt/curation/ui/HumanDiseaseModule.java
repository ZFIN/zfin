package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.TermEntry;
import org.zfin.gwt.root.ui.TermInfoComposite;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for FX curation module.
 */
public class HumanDiseaseModule implements HandlesError, EntryPoint {

    public static final String HUMAN_DISEASE_ZONE = "humanDiseaseZone";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("HumanDiseaseModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, HumanDiseaseModule> {
    }

    // data
    private String publicationID;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    AttributionModule attributionModule;
    @UiField
    DiseaseModelView diseaseModelView;
    @UiField
    TermInfoComposite termInfoBox;
    @UiField
    TermEntry termEntry;
    @UiField
    Button addButton;
    @UiField
    Button resetButton;
    @UiField
    SimpleErrorElement diseaseErrorLabel;

    private final HandlerManager eventBus = new HandlerManager(null);
    private DiseaseModelPresenter diseaseModelPresenter;

    public HumanDiseaseModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @UiHandler("resetButton")
    void onClickReset(@SuppressWarnings("unused") ClickEvent event) {
        termEntry.reset();
        diseaseErrorLabel.clearError();
    }

    @UiHandler("addButton")
    void onClickAdd(@SuppressWarnings("unused") ClickEvent event) {
        TermDTO disease = termInfoBox.getCurrentTermInfoDTO();
        eventBus.fireEvent(new AddNewDiseaseTermEvent(disease));
        diseaseModelPresenter.clearErrorMessages();
    }

    @Override
    public void onModuleLoad() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(HUMAN_DISEASE_ZONE).add(outer);
        diseaseModelPresenter = new DiseaseModelPresenter(eventBus, diseaseModelView, publicationID);
        diseaseModelPresenter.go();

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);

        bindEventBusHandler();
        addHandlerEvents();
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
        eventBus.addHandler(AddNewDiseaseTermEvent.TYPE,
                new AddNewDiseaseTermHandler() {
                    @Override
                    public void onAddDiseaseTerm(AddNewDiseaseTermEvent event) {
                        diseaseModelPresenter.addDiseaseToSelectionBox(event.getDisease());
                    }
                });
        eventBus.addHandler(ClickTermEvent.TYPE,
                new ClickTermEventHandler() {
                    @Override
                    public void onClickOnTerm(ClickTermEvent event) {
                        termEntry.setTerm(event.getTermDTO());
                        termInfoBox.reloadTermInfo(event.getTermDTO(), "termInfo");
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
