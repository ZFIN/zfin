package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.*;
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

    private String publicationID;
    private boolean debug;
    @UiField
    AttributionModule attributionModule;
    @UiField
    FeatureAddView featureAddView;
    @UiField
    FeatureEditView featureEditView;
    @UiField
    FeatureRelationshipView featureRelationshipView;

    private FeatureEditPresenter featureEditPresenter;
    private FeatureRelationshipPresenter featureRelationshipPresenter;

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
        FeatureAddPresenter addFeaturePresenter = new FeatureAddPresenter(featureAddView, publicationID);
        featureAddView.setPresenter(addFeaturePresenter);
        addFeaturePresenter.go();

        featureEditPresenter = new FeatureEditPresenter(featureEditView, publicationID);
        featureEditView.setPresenter(featureEditPresenter);
        featureEditPresenter.go();

        featureRelationshipPresenter = new FeatureRelationshipPresenter(featureRelationshipView, publicationID);
        featureRelationshipView.setPresenter(featureRelationshipPresenter);
        featureRelationshipPresenter.go();

        bindEventBusHandler();

    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(AddNewFeatureEvent.TYPE,
                new AddNewFeatureEventHandler() {
                    @Override
                    public void onAdd(AddNewFeatureEvent event) {
                        featureEditPresenter.loadFeaturesForPub(true);
                        featureRelationshipPresenter.onFeatureAddEvent();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddAttributeEvent.TYPE,
                new AddAttributeEventHandler() {
                    @Override
                    public void onEvent(AddAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        featureEditPresenter.loadFeaturesForPub(true);
                        featureRelationshipPresenter.onFeatureAddEvent();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        featureEditPresenter.loadFeaturesForPub(true);
                        featureRelationshipPresenter.onFeatureAddEvent();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(DirtyValueEvent.TYPE,
                new DirtyValueEventHandler() {
                    @Override
                    public void onDirtyEvent(DirtyValueEvent event) {
                        featureEditPresenter.onDirtyValueNotification(event.getDirty());
                    }
                });

    }


}
