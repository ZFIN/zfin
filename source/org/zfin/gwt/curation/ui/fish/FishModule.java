package org.zfin.gwt.curation.ui.fish;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.curation.event.CloneFishEvent;
import org.zfin.gwt.curation.event.CloneFishEventHandler;
import org.zfin.gwt.curation.event.CurationEvent;
import org.zfin.gwt.curation.event.EventType;
import org.zfin.gwt.curation.ui.ZfinCurationModule;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for FISH curation module.
 */
public class FishModule extends Composite implements ZfinCurationModule {

    public static final String FISH_TAB = "fishTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishModule.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, FishModule> {
    }

    private String publicationID;

    public FishModule(String publicationID) {
        this.publicationID = publicationID;
        init();
    }

    @UiField
    GenotypeView genotypeView;
    @UiField
    FishView fishView;
    @UiField
    FishGenotypeConstruction genotypeConstructionView;

    private GenotypePresenter presenter;
    private FishGenotypeConstructionPresenter genotypeConstructionPresenter;
    private FishPresenter fishPresenter;

    @Override
    public void init() {
        bindEventBusHandler();
        VerticalPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        presenter = new GenotypePresenter(genotypeView, publicationID);

        fishPresenter = new FishPresenter(fishView, publicationID);
        fishPresenter.go();
        genotypeConstructionPresenter = new FishGenotypeConstructionPresenter(genotypeConstructionView, publicationID);
        genotypeConstructionPresenter.go();
    }

    @Override
    public void refresh() {
        presenter.go();
        fishPresenter.go();
        genotypeConstructionPresenter.go();
    }

    @Override
    public void handleCurationEvent(CurationEvent event) {
        if (event.getEventType().is(EventType.MARKER_ATTRIBUTION))
            genotypeConstructionPresenter.updateStrList();
        if (event.getEventType().is(EventType.ADD_REMOVE_ATTRIBUTION_FEATURE) || event.getEventType().is(EventType.CUD_FEATURE)) {
            genotypeConstructionPresenter.updateFeatureList();
        }
        if (event.getEventType().is(EventType.ADD_REMOVE_ATTRIBUTION_FISH))
            fishPresenter.go();
        if (event.getEventType().is(EventType.ATTRIBUTE_GENOTYPE))
            presenter.go();
        if (event.getEventType().is(EventType.CUD_FISH)) {
            presenter.go();
            fishPresenter.go();
            genotypeConstructionView.resetGUI();
        }
        if (event.getEventType().is(EventType.CUD_FEATURE_RELATIONSHIP)) {
            genotypeConstructionPresenter.updateFeatureList();
        }
    }

    @Override
    public void handleTabToggle() {

    }

    @Override
    public void updateTermInfo(String termName, String ontologyName) {

    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(CloneFishEvent.TYPE,
                new CloneFishEventHandler() {
                    @Override
                    public void onClone(CloneFishEvent event) {
                        genotypeConstructionPresenter.populate(event.getFish());
                    }
                });

    }

}
