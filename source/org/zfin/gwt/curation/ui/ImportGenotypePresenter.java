package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.CheckBox;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class ImportGenotypePresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private final HandlerManager eventBus;
    private ImportGenotype view;
    private String publicationID;
    private List<GenotypeDTO> genotypeDTOList = new ArrayList<>();

    public ImportGenotypePresenter(HandlerManager eventBus, ImportGenotype view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
    }

    public void bind() {
        view.getShowHideSection().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                view.getSextionVisibilityToggle().toggleVisibility();
                retrieveInitialEntities();
            }
        });
        view.getSearchExistingGenotypes().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                searchForGenotypes();
            }
        });
        view.getFeatureListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchForGenotypes();
            }
        });
        view.getBackgroundListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                searchForGenotypes();
            }
        });
        bindAddGenotypeLinkHandler();
    }

    private void bindAddGenotypeLinkHandler() {
        final Map<CheckBox, GenotypeDTO> map = view.getGenotypeCheckboxMap();
        for (final CheckBox checkBox : map.keySet()) {
            checkBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    view.getLoadingImage().setVisible(true);
                    diseaseRpcService.addGenotypeToPublication(publicationID, map.get(checkBox).getZdbID(),
                            new ImportGenotypeCallBack("Import Genotype", view.getErrorLabel()));
                }
            });
        }
    }

    private void searchForGenotypes() {
        view.getErrorLabel().clearAllErrors();
        String featureID = getSelectedFeatureID();
        String genotypeID = getSelectedGenotypeID();
        if (featureID == null && genotypeID == null)
            return;
        diseaseRpcService.searchGenotypes(publicationID, featureID, genotypeID, new RetrieveExistingGenotypeListCallBack("error", view.getErrorLabel()));
        view.getLoadingImage().setVisible(true);
    }

    private String getSelectedFeatureID() {
        String id = view.getFeatureListBox().getSelectedValue();
        if (id.trim().equals(""))
            return null;
        return id;
    }

    private String getSelectedGenotypeID() {
        String id = view.getBackgroundListBox().getSelectedValue();
        if (id.trim().equals(""))
            return null;
        return id;
    }


    public void resetGUI() {
    }


    @Override
    public void go() {
        bind();
        view.updateExistingGenotypeListTableContent(null);
    }

    public void retrieveInitialEntities() {
        // get wildtype background list
        RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundCallback = new RetrieveRelatedEntityDTOListCallBack<>(view.getBackgroundListBox(), "Background List", view.getErrorLabel());
        retrieveBackgroundCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRpcService.getBackgroundGenotypes(publicationID, retrieveBackgroundCallback);
        updateFeatureList();
    }

    public void updateFeatureList() {
        // get Feature List
        RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureListCallBack = new RetrieveRelatedEntityDTOListCallBack<>(view.getFeatureListBox(), "Feature List", view.getErrorLabel());
        featureListCallBack.setLeaveFirstEntryBlank(true);
        diseaseRpcService.getFeatureList(publicationID, featureListCallBack);
    }

    class RetrieveExistingGenotypeListCallBack extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveExistingGenotypeListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.getLoadingImage());
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            resetGUI();
            if (list != null) {
                genotypeDTOList = list;
                view.updateExistingGenotypeListTableContent(list);
            }
            view.getLoadingImage().setVisible(false);
            bindAddGenotypeLinkHandler();
            if(list == null || list.size() == 0)
                view.getErrorLabel().setError("No more genotypes found");
        }
    }

    class ImportGenotypeCallBack extends ZfinAsyncCallback<GenotypeDTO> {

        public ImportGenotypeCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.getLoadingImage());
        }

        @Override
        public void onSuccess(GenotypeDTO genotypeDTO) {
            resetGUI();
            if (genotypeDTO != null) {
                view.getErrorLabel().setText("Imported genotype " + genotypeDTO.getHandle());
                genotypeDTOList.remove(genotypeDTO);
                view.updateExistingGenotypeListTableContent(genotypeDTOList);
                bindAddGenotypeLinkHandler();
            }
            view.getLoadingImage().setVisible(false);
            eventBus.fireEvent(new ImportGenotypeEvent());
        }
    }

}
