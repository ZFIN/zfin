package org.zfin.gwt.curation.ui;

import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Image;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.AppUtils;
import org.zfin.gwt.root.util.BooleanCollector;
import org.zfin.gwt.root.util.DeleteLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Table of associated genotypes
 */
public class FishGenotypeConstructionPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRpcService = CurationExperimentRPC.App.getInstance();
    private FishGenotypeConstruction view;
    private String publicationID;

    private List<GenotypeFeatureDTO> genotypeFeatureDTOList = new ArrayList<>(4);
    private List<ZygosityDTO> zygosityList = new ArrayList<>();
    private List<GenotypeDTO> backgroundGenoList = new ArrayList<>();
    private RetrieveSTRListCallBack strListCallBack;
    // only few STR will be added in most cases
    private Set<RelatedEntityDTO> newStrList = new TreeSet<>();

    public FishGenotypeConstructionPresenter(FishGenotypeConstruction view, String publicationID) {
        this.view = view;
        this.view.setPresenter(this);
        this.publicationID = publicationID;
        view.setPublicationID(publicationID);
    }

    public void bind() {
        strListCallBack = new RetrieveSTRListCallBack(view.strSelectionBox, "STRs", null);
    }

    public void onAddGenoFeatureClick() {
        int selectedFeature = view.getFeatureForGenotypeListBox().getSelectedIndex();
        GenotypeFeatureDTO dto = new GenotypeFeatureDTO();
        FeatureDTO feature = featureGenotypeListCallBack.getDtoList().get(selectedFeature);
        if (featureAlreadyInUse(feature)) {
            view.getErrorLabel().setError("Feature already used.");
            return;
        }
        if (isWildtypeWTSelected()) {
            view.getErrorLabel().setError("Cannot add a feature to a wildtype WT background.");
            return;
        }
        dto.setFeatureDTO(feature);
        dto.setZygosity(zygosityList.get(view.zygosityListBox.getSelectedIndex()));
        dto.setMaternalZygosity(zygosityList.get(view.zygosityMaternalListBox.getSelectedIndex()));
        dto.setPaternalZygosity(zygosityList.get(view.zygosityPaternalListBox.getSelectedIndex()));
        genotypeFeatureDTOList.add(dto);
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        // disable WT option as it should only be available when no feature is selected
        disableWTGenotypeBackground(true);
        handleDirty();
    }

    private boolean isWildtypeWTSelected() {
        for (GenotypeDTO background : backgroundGenoList) {
            if (background.getName().equals("WT"))
                return true;
        }
        return false;
    }

    protected void disableWTGenotypeBackground(boolean disable) {
        view.backgroundListBox.getElement().<SelectElement>cast().getOptions().getItem(1).setDisabled(disable);
    }

    protected void addRemoveGenotypeFeatureClickHandler(Anchor deleteAnchor, GenotypeFeatureDTO genotypeFeatureDTO) {
        deleteAnchor.addClickHandler(new RemoveGenotypeFeature(genotypeFeatureDTO));
    }

    private RetrieveRelatedEntityDTOListCallBack<GenotypeDTO> retrieveBackgroundNewGenoCallback;
    private RetrieveRelatedEntityDTOListCallBack<FeatureDTO> featureGenotypeListCallBack;

    private void resetNewGenotypeUI() {
        view.resetNewGentoypeUI();
        view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
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
        int index = view.backgroundListBox.getSelectedIndex();
        if (index == 0)
            return null;
        // offset empty first entry
        return retrieveBackgroundNewGenoCallback.getDtoList().get(index - 1);
    }

    private void resetError() {
        view.getErrorLabel().setError("");
        view.setMessage("");
    }

    public void resetGUI() {
        genotypeFeatureDTOList.clear();
        backgroundGenoList.clear();
        newStrList.clear();
        backgroundGenoList = new ArrayList<>();
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        view.backgroundListBox.setSelectedIndex(0);
        view.strSelectionBox.setSelectedIndex(0);
        view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        disableWTGenotypeBackground(false);
        view.createFishGenotypeButton.setEnabled(false);
    }

    @Override
    public void go() {
        bind();
        retrieveInitialEntities();
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList, newStrList);
    }

    public void retrieveInitialEntities() {
        // set wildtype background list for new Geno generation
        retrieveBackgroundNewGenoCallback = new RetrieveRelatedEntityDTOListCallBack<>(view.backgroundListBox, "Background List", view.getErrorLabel());
        retrieveBackgroundNewGenoCallback.setLeaveFirstEntryBlank(true);
        curationExperimentRpcService.getBackgroundGenotypes(publicationID, retrieveBackgroundNewGenoCallback);
        updateFeatureList();
        diseaseRpcService.getZygosityLists(new RetrieveZygosityListCallBack("Zygosity List", view.getErrorLabel()));
        updateStrList();
    }

    private void updateStrList() {
        diseaseRpcService.getStrList(publicationID, strListCallBack);
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
        diseaseRpcService.createGenotypeFish(publicationID,
                genotypeFeatureDTOList,
                backgroundGenoList,
                newStrList,
                new CreateGenotypeCallBack("Create new Genotype", view.getErrorLabel(), view.getLoadingImage()));
        view.getLoadingImage().setVisible(true);
    }

    public void onBackgroundClick() {
        backgroundGenoList.add(getSelectedGenotypeBackground());
        view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        handleDirty();
    }

    public void addDeleteGenotypeBackgroundClickHandler(DeleteLink deleteLink, final GenotypeDTO genotypeDTO) {
        deleteLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                backgroundGenoList.remove(genotypeDTO);
                view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
            }
        });
    }

    public void addDeleteStrClickHandler(Anchor removeLink, final RelatedEntityDTO str) {
        removeLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                newStrList.remove(str);
                view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
            }
        });
    }

    public void onStrClick(int index) {
        List<RelatedEntityDTO> strList = strListCallBack.getStrList();
        RelatedEntityDTO str = strList.get(index);
        if (!newStrList.contains(str)) {
            newStrList.add(str);
            view.reCreateStrPanel(newStrList);
            view.getErrorLabel().setText("");
        } else {
            view.getErrorLabel().setText("STR already added");
        }
        view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        handleDirty();
    }

    private void handleDirty() {
        BooleanCollector col = new BooleanCollector(true);
        col.addBoolean(newStrList.size() > 0);
        col.addBoolean(genotypeFeatureDTOList.size() > 0);
        col.addBoolean(backgroundGenoList.size() > 0);
        view.createFishGenotypeButton.setEnabled(col.arrivedValue());
    }

    public void update() {
        updateFeatureList();
        updateStrList();
    }

    public void populate(FishDTO fish) {
        // populate background
        backgroundGenoList.clear();
        if (fish.getGenotypeDTO().getBackgroundGenotypeList() != null && fish.getGenotypeDTO().getBackgroundGenotypeList().size() > 0) {
            for (GenotypeDTO bgGenotype : fish.getGenotypeDTO().getBackgroundGenotypeList()) {
                backgroundGenoList.add(bgGenotype);
            }
        }
        // populate STR list
        newStrList.clear();
        if (fish.getStrList() != null && fish.getStrList().size() > 0) {
            newStrList.addAll(fish.getStrList());
        }
        // if fish is a wildtype fish without features
        if (fish.getGenotypeDTO().isWildtype()) {
            backgroundGenoList.add(fish.getGenotypeDTO());
            view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
        }
        // populate geno features
        genotypeFeatureDTOList.clear();
        if (fish.getGenotypeDTO().getGenotypeFeatureList() != null) {
            genotypeFeatureDTOList.addAll(fish.getGenotypeDTO().getGenotypeFeatureList());
        }
        view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList, newStrList);
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
                    view.zygosityListBox.addItem(dto.getName(), dto.getZdbID());
                view.zygosityMaternalListBox.addItem(dto.getName(), dto.getZdbID());
                view.zygosityPaternalListBox.addItem(dto.getName(), dto.getZdbID());
            }
            view.on211ButtonClick(null);
        }
    }

    class CreateGenotypeCallBack extends ZfinAsyncCallback<GenotypeCreationReportDTO> {

        public CreateGenotypeCallBack(String errorMessage, ErrorHandler errorLabel, Image loadingImg) {
            super(errorMessage, errorLabel, loadingImg);
        }

        @Override
        public void onSuccess(GenotypeCreationReportDTO report) {
            resetNewGenotypeUI();
            view.getLoadingImage().setVisible(false);
            view.setMessage(report.getReportMessage());
            resetGUI();
            AppUtils.EVENT_BUS.fireEvent(new AddNewGenotypeEvent());
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
            if (genotypeFeatureDTOList.size() == 0)
                disableWTGenotypeBackground(false);
            view.updateGenotypeFeatureList(genotypeFeatureDTOList, backgroundGenoList, newStrList);
            view.setFishGenotypeInfo(genotypeFeatureDTOList, backgroundGenoList, newStrList);
            handleDirty();
        }
    }

    public class RetrieveSTRListCallBack extends ZfinAsyncCallback<List<RelatedEntityDTO>> {

        private StringListBox listBox;
        private List<RelatedEntityDTO> strList = new ArrayList<>();

        public RetrieveSTRListCallBack(StringListBox listBox, String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
            this.listBox = listBox;
        }

        @Override
        public void onSuccess(List<RelatedEntityDTO> dtoList) {
            listBox.clear();
            strList = new ArrayList<>();
            strList.add(new RelatedEntityDTO());
            strList.addAll(dtoList);

            listBox.addItem("------", "");
            for (RelatedEntityDTO entityDTO : dtoList) {
                listBox.addItem(entityDTO.getName(), entityDTO.getZdbID());
            }
        }

        public List<RelatedEntityDTO> getStrList() {
            return strList;
        }
    }

}
