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
    }


}
