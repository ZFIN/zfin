package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.AddNewFeatureEvent;
import org.zfin.gwt.curation.event.AddNewFeatureEventHandler;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for FX curation module.
 */
public class FeatureModule implements EntryPoint {

    public static final String FEATURE_ZONE = "feature-module";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureModule> {
    }

    // data
    private String publicationID;
    private boolean debug;
    @UiField
    AttributionModule attributionModule;
    @UiField
    FeatureAddView featureAddBox;
    @UiField
    FeatureEditView featureEditView;
    @UiField
    FeatureRelationshipBox featureRelationshipBox;

    private FeatureAddPresenter addFeaturePresenter;
    private FeatureEditPresenter featureEditPresenter;

    public FeatureModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @Override
    public void onModuleLoad() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FEATURE_ZONE).add(outer);

        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        addFeaturePresenter = new FeatureAddPresenter(featureAddBox, publicationID);
        featureAddBox.setPresenter(addFeaturePresenter);
        addFeaturePresenter.go();

        featureEditPresenter = new FeatureEditPresenter(featureEditView, publicationID);
        featureEditView.setPresenter(featureEditPresenter);
        featureEditPresenter.go();

/*
        curationFilterPresenter = new CurationFilterPresenter(curationFilterZone, publicationID);
        curationFilterPresenter.go();
        curationFilterZone.setCurationFilterPresenter(curationFilterPresenter);

        fxCurationPresenter = new FxCurationPresenter(constructionZoneModule, publicationID);
        fxCurationPresenter.go();
        constructionZoneModule.setFxCurationPresenter(fxCurationPresenter);

        structurePilePresenter = new StructurePilePresenter(structurePile, publicationID);
        structurePilePresenter.go();
        structurePile.setStructurePilePresenter(structurePilePresenter);

        ExperimentDTO dto = new ExperimentDTO();
        dto.setPublicationID(publicationID);
        expressionZone.setExperimentFilter(dto);
        structurePile.getStructurePileTable().setExpressionSection(expressionZone);

        expressionExperimentZonePresenter = new ExpressionExperimentZonePresenter(expressionExperimentZone, publicationID, debug);
        expressionExperimentZonePresenter.go();

        expressionZonePresenter = new ExpressionZonePresenter(expressionZone, publicationID);
        expressionZonePresenter.go();
        expressionZone.setExpressionZonePresenter(expressionZonePresenter);
        expressionZone.setStructurePilePresenter(structurePilePresenter);
        expressionZone.setExpressionExperimentZonePresenter(expressionExperimentZonePresenter);
*/

        bindEventBusHandler();

    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(AddNewFeatureEvent.TYPE,
                new AddNewFeatureEventHandler() {
                    @Override
                    public void onAdd(AddNewFeatureEvent event) {
                        featureEditPresenter.loadFeaturesForPub();
                    }
                });
/*
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
                        expressionZonePresenter.postUpdateStructuresOnExpression();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(ChangeCurationFilterEvent.TYPE,
                new ChangeCurationFilterEventHandler() {
                    @Override
                    public void onChange(ChangeCurationFilterEvent event) {
                        expressionZonePresenter.updateExpressionOnCurationFilter(event.getExperimentFilter(), event.getFigureID());
                        expressionExperimentZonePresenter.updateExperimentOnCurationFilter(event.getExperimentFilter());
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddExpressionExperimentEvent.TYPE,
                new AddExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(AddExpressionExperimentEvent event) {
                        expressionExperimentZonePresenter.notifyAddedExpression();
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
        AppUtils.EVENT_BUS.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        expressionExperimentZonePresenter.updateGenes();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddAttributeEvent.TYPE,
                new AddAttributeEventHandler() {
                    @Override
                    public void onEvent(AddAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        expressionExperimentZonePresenter.updateGenes();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveExpressionExperimentEvent.TYPE,
                new RemoveExpressionExperimentEventHandler() {
                    @Override
                    public void onEvent(RemoveExpressionExperimentEvent event) {
                        expressionZonePresenter.retrieveExpressions();
                    }
                });
*/
    }


}
