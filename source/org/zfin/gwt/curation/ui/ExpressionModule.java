package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.*;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.ui.ZfinModule;
import org.zfin.gwt.root.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for FX curation module.
 */
public class ExpressionModule implements ZfinCurationModule, HandlesError {

    private static final String EXPRESSION_ZONE = "expressionZone";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExpressionModule> {
    }

    // data
    private String publicationID;
    private boolean debug;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    ConstructionZoneModule constructionZoneModule;
    @UiField
    StructurePileView structurePile;
    @UiField
    ExpressionZoneView expressionZone;
    @UiField
    ExpressionExperimentZoneView expressionExperimentZone;
    @UiField
    CurationFilterView curationFilterZone;

    private FxCurationPresenter fxCurationPresenter;
    private StructurePilePresenter structurePilePresenter;
    private ExpressionZonePresenter expressionZonePresenter;
    private ExpressionExperimentZonePresenter expressionExperimentZonePresenter;
    private CurationFilterPresenter curationFilterPresenter;

    ExpressionModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    @Override
    public void init() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(EXPRESSION_ZONE).add(outer);

        curationFilterPresenter = new CurationFilterPresenter(curationFilterZone, publicationID);
        curationFilterPresenter.go();
        curationFilterZone.setCurationFilterPresenter(curationFilterPresenter);

        fxCurationPresenter = new FxCurationPresenter(constructionZoneModule, publicationID);
        fxCurationPresenter.go();
        constructionZoneModule.setFxCurationPresenter(fxCurationPresenter);

        structurePilePresenter = new StructurePilePresenter(structurePile, publicationID);
        structurePilePresenter.go();
        structurePile.setStructurePilePresenter(structurePilePresenter);

        ExpressionExperimentDTO dto = new ExpressionExperimentDTO();
        dto.setPublicationID(publicationID);
        structurePile.getStructurePileTable().setExpressionSection(expressionZone);

        expressionExperimentZonePresenter = new ExpressionExperimentZonePresenter(expressionExperimentZone, publicationID, debug);
        expressionExperimentZonePresenter.go();

        expressionZonePresenter = new ExpressionZonePresenter(expressionZone, publicationID);
        expressionZonePresenter.go();
        expressionZone.setExpressionZonePresenter(expressionZonePresenter);
        expressionZone.setStructurePilePresenter(structurePilePresenter);
        expressionZone.setExpressionExperimentZonePresenter(expressionExperimentZonePresenter);

        bindEventBusHandler();
        addHandlerEvents();
    }

    @Override
    public void refresh() {
        curationFilterPresenter.go();
        fxCurationPresenter.go();
        expressionExperimentZonePresenter.go();
        expressionZonePresenter.go();
    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {
        fxCurationPresenter.updateTermInfo(termName, ontologyName);
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event.getEventType().is(EventType.FILTER)) {
            ChangeCurationFilterEvent changeEvent = (ChangeCurationFilterEvent) event;
            expressionZonePresenter.updateExpressionOnCurationFilter(changeEvent.getExperimentFilter(), changeEvent.getFigureID());
            expressionExperimentZonePresenter.updateExperimentOnCurationFilter(changeEvent.getExperimentFilter());
            if (event.getEventType().is(EventType.PHENO_TAB)) {
                curationFilterPresenter.setFilterValues(changeEvent.getExperimentFilter(), changeEvent.getFigureID());
            }
        }
        if (event.getEventType().is(EventType.MARKER)) {
            expressionExperimentZonePresenter.updateGenes();
            expressionExperimentZonePresenter.updateAntibodyList();
            curationFilterPresenter.go();
        }
        if (event.getEventType().is(EventType.FISH)) {
            expressionExperimentZonePresenter.retrieveFishList();
            curationFilterPresenter.go();
        }
        if (event.getEventType().is(EventType.CUD_EXPERIMENT_CONDITION)) {
            expressionExperimentZonePresenter.updateEnvironmentList();
        }
        if (event.getEventType().is(EventType.REMOVE_PHENTOTYPE_EXPERIMENT)) {
            // update possible push-to-pato reversals
            expressionZonePresenter.retrieveExpressions();
        }
        if (event.getEventType().is(EventType.MARKER_ATTRIBUTION) || event.getEventType().is(EventType.MARKER_DEATTRIBUTION)) {
            expressionExperimentZonePresenter.updateGenes();
            curationFilterPresenter.refreshGeneList();
        }
        if (event.getEventType().is(EventType.ADD_REMOVE_ATTRIBUTION_FISH)) {
            expressionExperimentZonePresenter.retrieveFishList();
        }
        if (event.getEventType().is(EventType.ADD_FIGURE)) {
            expressionZonePresenter.refreshFigure();
            curationFilterPresenter.refreshFigureList();
        }
    }

    @Override
    public void handleTabToggle() {

    }

    public void addHandlerEvents() {
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
        AppUtils.EVENT_BUS.addHandler(SelectExpressionEvent.TYPE,
                new SelectExpressionEventHandler() {
                    @Override
                    public void onEvent(SelectExpressionEvent event) {
                        structurePilePresenter.updateFigureAnnotations(event.getSelectedExpressions());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(SelectExpressionExperimentEvent.TYPE,
                new SelectExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(SelectExpressionExperimentEvent event) {
                        if (!event.isCkecked())
                            expressionExperimentZonePresenter.unselectExperiment(event.getExperimentDTO());
                        else
                            expressionExperimentZonePresenter.setSingleExperiment(event.getExperimentDTO());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(CreateExpressionEvent.TYPE,
                new CreateExpressionEventHandler() {
                    @Override
                    public void onEvent(CreateExpressionEvent event) {
                        expressionZonePresenter.postUpdateStructuresOnExpression(event.getFigureStageDTOList());
                        expressionExperimentZonePresenter.finishExpressionNotification();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddExpressionExperimentEvent.TYPE,
                new AddExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(AddExpressionExperimentEvent event) {
                        expressionExperimentZonePresenter.notifyAddedExpression(event.getExpressionExperimentDTOMap());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveExpressionEvent.TYPE,
                new RemoveExpressionEventHandler() {
                    @Override
                    public void onEvent(RemoveExpressionEvent event) {
                        expressionExperimentZonePresenter.notifyRemovedExpression(event.getExperimentDTO());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(UpdateExpressionExperimentEvent.TYPE,
                new UpdateExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(UpdateExpressionExperimentEvent event) {
                        expressionZonePresenter.retrieveExpressions();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveExpressionExperimentEvent.TYPE,
                new RemoveExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(RemoveExpressionExperimentEvent event) {
                        expressionZonePresenter.retrieveExpressions();
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

    public static ZfinModule getModuleInfo() {
        return new ZfinModule(CurationTab.FX.getName(), ExpressionModule.class.getName());
    }
}
