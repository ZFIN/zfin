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
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.util.AppUtils;

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
    @UiField
    StructurePileModule structurePile;
    @UiField
    ExpressionZoneView expressionZone;

    private FxCurationPresenter fxCurationPresenter;
    private StructurePilePresenter structurePilePresenter;
    private ExpressionZonePresenter expressionZonePresenter;
    FxCurationModule fxCurationModule;

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

        fxCurationPresenter = new FxCurationPresenter(constructionZoneModule, publicationID);
        fxCurationPresenter.go();
        constructionZoneModule.setFxCurationPresenter(fxCurationPresenter);

        structurePilePresenter = new StructurePilePresenter(structurePile, publicationID);
        structurePilePresenter.go();

        expressionZonePresenter = new ExpressionZonePresenter(expressionZone, publicationID);
        expressionZonePresenter.go();
        ExperimentDTO dto = new ExperimentDTO();
        dto.setPublicationID(publicationID);
        expressionZone.setExperimentFilter(dto);

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
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {

            }
        });
    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(ExpressionEvent.TYPE,
                new ExpressionEventHandler() {
                    @Override
                    public void onAddStructures(ExpressionEvent event) {
                        structurePilePresenter.onPileStructureCreation(event.getStructureList());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(ClickStructureOnPileEvent.TYPE,
                new ClickStructureOnPileHandler() {
                    @Override
                    public void onEvent(ClickStructureOnPileEvent event) {
                        fxCurationPresenter.prepopulateConstructionZone(event.getExpressedTerm(), event.getPileEntity());
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
