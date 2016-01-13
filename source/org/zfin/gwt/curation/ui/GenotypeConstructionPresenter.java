package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.GenotypeDTO;
import org.zfin.gwt.root.dto.GenotypeFeatureDTO;
import org.zfin.gwt.root.dto.ZygosityDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteLink;

import java.util.ArrayList;
import java.util.List;

/**
 * Table of associated genotypes
 */
public class GenotypeConstructionPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private final HandlerManager eventBus;
    private GenotypeConstruction view;
    private String publicationID;

    private List<GenotypeFeatureDTO> genotypeFeatureDTOList = new ArrayList<>(4);
    private List<ZygosityDTO> zygosityList = new ArrayList<>();
    private List<GenotypeDTO> backgroundGenoList = new ArrayList<>();

    public GenotypeConstructionPresenter(HandlerManager eventBus, GenotypeConstruction view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.view.setPresenter(this);
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
    }

    public void bind() {
        view.getAddGenotypeFeature().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                resetError();
                int selectedFeature = view.getFeatureForGenotypeListBox().getSelectedIndex();
                GenotypeFeatureDTO dto = new GenotypeFeatureDTO();
                FeatureDTO feature = featureGenotypeListCallBack.getDtoList().get(selectedFeature);
                if (featureAlreadyInUse(feature)) {
                    view.getErrorLabel().setError("Feature already used.");
                    return;
                }
                dto.setFeatureDTO(feature);
                dto.setZygosity(zygosityList.get(view.getZygosityListBox().getSelectedIndex()));
                dto.setMaternalZygosity(zygosityList.get(view.getZygosityMaternalListBox().getSelectedIndex()));
                dto.setPaternalZygosity(zygosityList.get(view.getZygosityPaternalListBox().getSelectedIndex()));
                genotypeFeatureDTOList.add(dto);
                view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList);
            }
        });
        view.getFeatureForGenotypeListBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                resetError();
            }
        });
    }

    protected void addRemoveGenotypeFeatureClickHandler(Anchor deleteAnchor, GenotypeFeatureDTO genotypeFeatureDTO) {
        deleteAnchor.addClickHandler(new RemoveGenotypeFeature(genotypeFeatureDTO));
    }

    private RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundNewGenoCallback;
    private RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureGenotypeListCallBack;

    private void resetNewGenotypeUI() {
        view.resetNewGentoypeUI();
        view.setGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList);
    }


    private boolean featureAlreadyInUse(FeatureDTO feature) {
        if (genotypeFeatureDTOList == null)
            return false;
        for (GenotypeFeatureDTO dto : genotypeFeatureDTOList)
            if (dto.getFeatureDTO().equals(feature))
                return true;
        return false;
    }

    private GenotypeDTO getSelectedGenotypeBackground() {
        int index = view.getBackgroundListBox().getSelectedIndex();
        if (index == 0)
            return null;
        // offset empty first entry
        return retrieveBackgroundNewGenoCallback.getDtoList().get(index - 1);
    }

    private void resetError() {
        view.getErrorLabel().setError("");
    }

    public void resetGUI() {
        genotypeFeatureDTOList.clear();
        backgroundGenoList = new ArrayList<>();
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList);
        view.getBackgroundListBox().setSelectedIndex(0);
        view.setGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList);
    }

    @Override
    public void go() {
        bind();
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList);
    }

    public void retrieveInitialEntities() {
        // set wildtype background list for new Geno generation
        retrieveBackgroundNewGenoCallback = new RetrieveRelatedEntityDTOListCallBack<>(view.getBackgroundListBox(), "Background List", view.getErrorLabel());
        retrieveBackgroundNewGenoCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRpcService.getBackgroundGenotypes(publicationID, retrieveBackgroundNewGenoCallback);
        updateFeatureList();
        diseaseRpcService.getZygosityLists(new RetrieveZygosityListCallBack("Zygosity List", view.getErrorLabel()));
    }

    public void updateFeatureList() {
        // get Feature List  from new Genotypes
        featureGenotypeListCallBack = new RetrieveRelatedEntityDTOListCallBack<>(view.getFeatureForGenotypeListBox(), "Feature Geno List", view.getErrorLabel());
        diseaseRpcService.getFeatureList(publicationID, featureGenotypeListCallBack);
    }

    public void onShowHideClick() {
        view.getGenotypeConstructionToggle().toggleVisibility();
        retrieveInitialEntities();
    }

    public void onCreateGenotypeButtonClick() {
        resetError();
        if (genotypeFeatureDTOList.size() == 0) {
            view.getErrorLabel().setError("No Feature selected");
            return;
        }
        String nicknameString = view.getGenotypeNickname().getText();
        if (nicknameString.equals(view.getGenotypeHandle().getText()))
            nicknameString = null;
        diseaseRpcService.createGenotypeFeature(publicationID,
                genotypeFeatureDTOList,
                backgroundGenoList,
                nicknameString,
                new CreateGenotypeCallBack("Create new Genotype", view.getErrorLabel(), view.getLoadingImage()));
        view.getLoadingImage().setVisible(true);
    }

    public void onResetClick() {
        resetGUI();
    }

    public void onBackgroundClick() {
        backgroundGenoList.add(getSelectedGenotypeBackground());
        view.setGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList);
        resetError();
    }

    public void on211Click() {
        view.getZygosityListBox().setSelectedIndex(0);
        view.getZygosityMaternalListBox().setSelectedIndex(1);
        view.getZygosityPaternalListBox().setSelectedIndex(1);
        resetError();
    }

    public void on2UUClick() {
        view.getZygosityListBox().setSelectedIndex(0);
        view.getZygosityMaternalListBox().setSelectedIndex(3);
        view.getZygosityPaternalListBox().setSelectedIndex(3);
        resetError();
    }

    public void onUUUClick() {
        view.getZygosityListBox().setSelectedIndex(3);
        view.getZygosityMaternalListBox().setSelectedIndex(3);
        view.getZygosityPaternalListBox().setSelectedIndex(3);
        resetError();
    }

    public void addDeleteGenotypeBackgroundClickHandler(DeleteLink deleteLink, final GenotypeDTO genotypeDTO) {
        deleteLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                backgroundGenoList.remove(genotypeDTO);
                view.setGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList);
            }
        });
    }

    class RetrieveZygosityListCallBack extends ZfinAsyncCallback<List<ZygosityDTO>> {

        public RetrieveZygosityListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.getLoadingImage());
        }

        @Override
        public void onSuccess(List<ZygosityDTO> list) {
            zygosityList = list;
            for (ZygosityDTO dto : list) {
                if (!dto.getName().startsWith("wild"))
                    view.getZygosityListBox().addItem(dto.getName(), dto.getZdbID());
                view.getZygosityMaternalListBox().addItem(dto.getName(), dto.getZdbID());
                view.getZygosityPaternalListBox().addItem(dto.getName(), dto.getZdbID());
            }
            on211Click();
        }
    }

    class CreateGenotypeCallBack extends ZfinAsyncCallback<GenotypeDTO> {

        public CreateGenotypeCallBack(String errorMessage, ErrorHandler errorLabel, Image loadingImg) {
            super(errorMessage, errorLabel, loadingImg);
        }

        @Override
        public void onSuccess(GenotypeDTO genotypeDTO) {
            resetNewGenotypeUI();
            view.getLoadingImage().setVisible(false);
            errorHandler.setError("Created new Genotype: " + genotypeDTO.getHandle());
            resetGUI();
            eventBus.fireEvent(new AddNewGenotypeEvent());
        }
    }

    class RemoveGenotypeFeature implements ClickHandler {

        private GenotypeFeatureDTO dto;

        public RemoveGenotypeFeature(GenotypeFeatureDTO dto) {
            this.dto = dto;
        }

        @Override
        public void onClick(ClickEvent clickEvent) {
            genotypeFeatureDTOList.remove(dto);
            view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList);
        }
    }


}
