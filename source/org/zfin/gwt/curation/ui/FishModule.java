package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.zfin.gwt.curation.event.AddAttributeEvent;
import org.zfin.gwt.curation.event.AddAttributeEventHandler;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;
import org.zfin.gwt.root.util.AppUtils;

/**
 * Entry point for FISH curation module.
 */
public class FishModule extends Composite implements EntryPoint {

    public static final String FISH_TAB = "fishTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishModule.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, FishModule> {
    }

    private String publicationID;

    private AttributionModule attributionModule;

    public FishModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @UiField
    GenotypeView genotypeView;
    @UiField
    ImportGenotype importGenotypeView;
    @UiField
    FishView fishView;
    @UiField
    FishConstruction fishConstructionView;
    @UiField
    GenotypeConstruction genotypeConstructionView;

    private GenotypePresenter presenter;
    private ImportGenotypePresenter importPresenter;
    private GenotypeConstructionPresenter genotypeConstructionPresenter;
    private FishConstructionPresenter fishConstructionPresenter;
    private FishPresenter fishPresenter;

    @Override
    public void onModuleLoad() {
        bindEventBusHandler();
        VerticalPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        addHandlers();
        presenter = new GenotypePresenter(genotypeView, publicationID);
        presenter.go();
        importPresenter = new ImportGenotypePresenter(importGenotypeView, publicationID);
        importPresenter.go();
        importGenotypeView.setPresenter(importPresenter);

        fishPresenter = new FishPresenter(fishView, publicationID);
        fishPresenter.go();
        fishConstructionPresenter = new FishConstructionPresenter(fishConstructionView, publicationID);
        fishConstructionPresenter.setFishPresenter(fishPresenter);
        fishConstructionPresenter.go();
        genotypeConstructionPresenter = new GenotypeConstructionPresenter(genotypeConstructionView, publicationID);
        genotypeConstructionPresenter.go();
    }

    private void bindEventBusHandler() {
        AppUtils.EVENT_BUS.addHandler(AddNewFishEvent.TYPE,
                new AddNewFishEventHandler() {
                    @Override
                    public void onAddFish(AddNewFishEvent event) {
                        fishPresenter.go();
                        attributionModule.populateAttributeRemoval();
                        importGenotypeView.resetGUI();
                        genotypeConstructionView.resetGUI();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddNewGenotypeEvent.TYPE,
                new AddNewGenotypeEventHandler() {
                    @Override
                    public void onAddGenotype(AddNewGenotypeEvent event) {
                        presenter.go();
                        fishConstructionPresenter.updateGenotypeList();
                        attributionModule.populateAttributeRemoval();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        importPresenter.updateFeatureList();
                        presenter.go();
                        genotypeConstructionPresenter.updateFeatureList();
                        fishConstructionPresenter.retrieveInitialEntities();
                        attributionModule.populateAttributeRemoval();
                        fishPresenter.go();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(ImportGenotypeEvent.TYPE,
                new ImportGenotypeEventHandler() {
                    @Override
                    public void onImportGenotype(ImportGenotypeEvent event) {
                        presenter.go();
                        fishConstructionPresenter.updateGenotypeList();
                        attributionModule.populateAttributeRemoval();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        importPresenter.updateFeatureList();
                        genotypeConstructionPresenter.updateFeatureList();
                        fishConstructionPresenter.updateSTRListBox();
                    }
                });
        AppUtils.EVENT_BUS.addHandler(AddAttributeEvent.TYPE,
                new AddAttributeEventHandler() {
                    @Override
                    public void onEvent(AddAttributeEvent event) {
                        attributionModule.populateAttributeRemoval();
                        importPresenter.updateFeatureList();
                        genotypeConstructionPresenter.updateFeatureList();
                        fishConstructionPresenter.updateSTRListBox();
                    }
                });

    }

    private void addHandlers() {
        attributionModule.addHandlesErrorListener(new HandlesError() {
            @Override
            public void setError(String message) {
            }

            @Override
            public void clearError() {
                fireEventSuccess();
            }

            @Override
            public void fireEventSuccess() {
                // TODo: Need to handle removal additions separately
                AppUtils.EVENT_BUS.fireEvent(new RemoveAttributeEvent());
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {
            }
        });

    }

}
