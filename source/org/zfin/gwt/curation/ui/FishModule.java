package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.ui.HandlesError;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for FX curation module.
 */
public class FishModule extends Composite implements EntryPoint {

    public static final String FISH_TAB = "fishTab";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FishModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FishModule> {
    }

    // data
    private String publicationID;

    private AttributionModule attributionModule;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    Image loadingImage;

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

    private final HandlerManager eventBus = new HandlerManager(null);

    @Override
    public void onModuleLoad() {
        bindEventBusHandler();
        FlowPanel outer = uiBinder.createAndBindUi(this);
        RootPanel.get(FISH_TAB).add(outer);
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
        addHandlers();
        GenotypePresenter presenter = new GenotypePresenter(eventBus, genotypeView, publicationID);
        presenter.go();
        ImportGenotypePresenter importPresenter = new ImportGenotypePresenter(eventBus, importGenotypeView, publicationID);
        importPresenter.go();
        FishPresenter fishPresenter = new FishPresenter(eventBus, fishView, publicationID);
        fishPresenter.go();
        FishConstructionPresenter fishConstructionPresenter = new FishConstructionPresenter(eventBus, fishConstructionView, publicationID);
        fishConstructionPresenter.go();
        fishConstructionPresenter.setFishPresenter(fishPresenter);
        GenotypeConstructionPresenter genotypeConstructionPresenter = new GenotypeConstructionPresenter(eventBus, genotypeConstructionView, publicationID);
        genotypeConstructionPresenter.go();
    }

    private void bindEventBusHandler() {
        eventBus.addHandler(AddNewFishEvent.TYPE,
                new AddNewFishEventHandler() {
                    @Override
                    public void onAddFish(AddNewFishEvent event) {
                        FishPresenter presenter = new FishPresenter(eventBus, fishView, publicationID);
                        presenter.go();
                    }
                });
        eventBus.addHandler(AddNewGenotypeEvent.TYPE,
                new AddNewGenotypeEventHandler() {
                    @Override
                    public void onAddGenotype(AddNewGenotypeEvent event) {
                        GenotypePresenter presenter = new GenotypePresenter(eventBus, genotypeView, publicationID);
                        presenter.go();
                        FishConstructionPresenter constructionPresenter = new FishConstructionPresenter(eventBus, fishConstructionView, publicationID);
                        constructionPresenter.retrieveInitialEntities();
                    }
                });
        eventBus.addHandler(RemoveAttributeEvent.TYPE,
                new RemoveAttributeEventHandler() {
                    @Override
                    public void onRemoveAttribute(RemoveAttributeEvent event) {
                        Window.alert("RemoveAttr");
                        GenotypePresenter genotypePresenter = new GenotypePresenter(eventBus, genotypeView, publicationID);
                        genotypePresenter.go();
                        FishPresenter presenter = new FishPresenter(eventBus, fishView, publicationID);
                        presenter.go();
                        Window.alert("RemoveAttr after");
                        FishConstructionPresenter constructionPresenter = new FishConstructionPresenter(eventBus, fishConstructionView, publicationID);
                        constructionPresenter.retrieveInitialEntities();
                    }
                });
        eventBus.addHandler(ImportGenotypeEvent.TYPE,
                new ImportGenotypeEventHandler() {
                    @Override
                    public void onImportGenotype(ImportGenotypeEvent event) {
                        GenotypePresenter presenter = new GenotypePresenter(eventBus, genotypeView, publicationID);
                        presenter.go();
                        FishConstructionPresenter constructionPresenter = new FishConstructionPresenter(eventBus, fishConstructionView, publicationID);
                        constructionPresenter.retrieveInitialEntities();
                        attributionModule.populateAttributeRemoval();
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
                eventBus.fireEvent(new RemoveAttributeEvent());
            }

            @Override
            public void addHandlesErrorListener(HandlesError handlesError) {
            }
        });

    }

}
