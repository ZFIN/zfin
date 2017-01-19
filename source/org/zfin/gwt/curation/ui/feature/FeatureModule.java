package org.zfin.gwt.curation.ui.feature;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.curation.event.*;
import org.zfin.gwt.curation.ui.ZfinCurationModule;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for FX curation module.
 */
public class FeatureModule implements ZfinCurationModule {

    public static final String FEATURE_ZONE = "feature-module";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureModule> {
    }

    private String publicationID;
    private boolean debug;
    @UiField
    FeatureAddView featureAddView;
    @UiField
    FeatureEditView featureEditView;
    @UiField
    FeatureRelationshipView featureRelationshipView;

    private FeatureEditPresenter featureEditPresenter;
    private FeatureRelationshipPresenter featureRelationshipPresenter;
    private FeatureAddPresenter addFeaturePresenter;

    public FeatureModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    @Override
    public void init() {
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FEATURE_ZONE).add(outer);

        addFeaturePresenter = new FeatureAddPresenter(featureAddView, publicationID);
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

    public void refresh() {
        featureEditPresenter.refresh();
        featureRelationshipPresenter.go();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event == null)
            return;
        EventType eventType = event.getEventType();
        if (eventType.is(EventType.ATTRIBUTE_MARKER) || eventType.is(EventType.DEATTRIBUTE_MARKER)) {
            featureRelationshipView.onChangeFeatureRelationship(null);
        }
        if (eventType.is(EventType.ADD_REMOVE_ATTRIBUTION_FEATURE) || eventType.is(EventType.CREATE_FEATURE)) {
            featureEditPresenter.loadFeaturesForPub(true);
            featureRelationshipPresenter.onFeatureAddEvent();
        }
    }

    @Override
    public void handleTabToggle() {

    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {

    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(DirtyValueEvent.TYPE,
                new DirtyValueEventHandler() {
                    @Override
                    public void onDirtyEvent(DirtyValueEvent event) {
                        featureEditPresenter.onDirtyValueNotification(event.getDirty());
                    }
                });

    }


}
