package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Hyperlink;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;
import org.zfin.gwt.root.util.DeleteImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class DiseaseModelPresenter implements Presenter {

    private final HandlerManager eventBus;
    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationRPCService = CurationExperimentRPC.App.getInstance();
    private DiseaseModelView view;
    private String publicationID;
    private List<FishDTO> fishList = new ArrayList<>();
    private List<EnvironmentDTO> environmentList = new ArrayList<>();
    private List<TermDTO> diseaseList = new ArrayList<>();
    private List<DiseaseAnnotationDTO> diseaseModelList = new ArrayList<>();
    private boolean processing = false;

    public DiseaseModelPresenter(HandlerManager eventBus, DiseaseModelView view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
    }

    public void bind() {
        view.getFishSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
                updateConditions();

                //view.getEnvironmentSelectionBox().removeItem(0);
            }
        });
        view.getEnvironmentSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        addDynamicClickHandler();
    }

    protected void addModelEvent() {
        if (processing)
            return;
        processing = true;
        DiseaseAnnotationDTO disease = getDiseaseModel();

        if (disease == null) {
            processing = false;
            return;
        }

        diseaseRpcService.addHumanDiseaseAnnotation(disease, new RetrieveDiseaseModelListCallBack("Could not add a new disease model", view.getErrorLabel()));
        view.getLoadingImage().setVisible(true);
    }

    private DiseaseAnnotationDTO getDiseaseModel() {
        DiseaseAnnotationDTO dto = new DiseaseAnnotationDTO();
        int selectedIndexFish = view.getFishSelectionBox().getSelectedIndex();
        int selectedIndexEnv = view.getEnvironmentSelectionBox().getSelectedIndex();
        if ((selectedIndexFish == 0 && selectedIndexEnv > 0) ||
                (selectedIndexFish > 0 && selectedIndexEnv == 0)) {
            setError("You need to select both a Fish and an Environment or none at all.");
            return null;
        }
        if (selectedIndexEnv > 0 && selectedIndexFish > 0) {
            dto.setFish(fishList.get(selectedIndexFish - 1));
            dto.setEnvironment(environmentList.get(selectedIndexEnv - 1));
        }
        int selectedIndexDis = view.getDiseaseSelectionBox().getSelectedIndex();
        if (selectedIndexDis == -1) {
            setError("No Disease available. Please add a new disease below.");
            return null;
        }
        dto.setDisease(diseaseList.get(selectedIndexDis));
        String itemText = view.getEvidenceCodeSelectionBox().getItemText(view.getEvidenceCodeSelectionBox().getSelectedIndex());
        dto.setEvidenceCode(itemText);
        PublicationDTO pubDto = new PublicationDTO();
        pubDto.setZdbID(publicationID);
        dto.setPublication(pubDto);
        return dto;

    }

    private void addDynamicClickHandler() {
        Map<DeleteImage, DiseaseAnnotationDTO> map = view.getDeleteModeMap();
        for (DeleteImage deleteButton : map.keySet()) {
            deleteButton.addClickHandler(new HumanDiseaseModelDeleteClickListener(map.get(deleteButton)));
        }
        Map<DeleteImage, DiseaseAnnotationModelDTO> map1 = view.getDeleteModeMap1();
        for (DeleteImage deleteImage : map1.keySet()) {
            deleteImage.addClickHandler(new HumanDiseaseAnnotationModelDeleteClickListener(map1.get(deleteImage)));
        }

        Map<Hyperlink, DiseaseAnnotationDTO> linkMap = view.getTermLinkDiseaseModelMap();
        for (Hyperlink link : linkMap.keySet()) {
            link.addClickHandler(new PopulateTermEntryClickListener(linkMap.get(link).getDisease()));
        }
    }


    @Override
    public void go() {
        bind();
        setEvidenceCode();
        createFishModelList();
    }

    private void createFishModelList() {
        // human disease model list
        diseaseRpcService.getHumanDiseaseModelList(publicationID, new RetrieveDiseaseModelListCallBack(null, view.getErrorLabel()));

        // environment list
        String message = "Error while reading the environment";
        curationRPCService.getEnvironments(publicationID, new RetrieveEnvironmentListCallBack(message, view.getErrorLabel()));

        updateFishList();
    }

    private void setEvidenceCode() {
        view.getEvidenceCodeSelectionBox().addItem("TAS");
        view.getEvidenceCodeSelectionBox().addItem("IC");
        view.getEvidenceCodeSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        view.getEvidenceCodeSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
    }


    private void updateDiseaseList(List<DiseaseAnnotationDTO> diseaseModelList) {
        if (diseaseModelList == null)
            return;

        for (DiseaseAnnotationDTO annotationDTO : diseaseModelList) {
            if (!diseaseList.contains(annotationDTO.getDisease()))
                diseaseList.add(annotationDTO.getDisease());
        }
        reCreateDiseaseListBox();
    }

    private void reCreateDiseaseListBox() {
        Collections.sort(diseaseList);
        view.getDiseaseSelectionBox().clear();
        for (TermDTO disease : diseaseList)
            view.getDiseaseSelectionBox().addItem(disease.getTermName(), disease.getZdbID());
    }

    public void setError(String message) {
        view.getErrorLabel().setText(message);
    }

    public void clearErrorMessages() {
        view.getErrorLabel().setError("");
    }

    private void resetUI() {
        view.getErrorLabel().clearAllErrors();
        view.getFishSelectionBox().setSelectedIndex(0);
        view.getEnvironmentSelectionBox().setSelectedIndex(0);
        view.getDiseaseSelectionBox().setSelectedIndex(0);
        view.getEvidenceCodeSelectionBox().setSelectedIndex(0);
        clearErrorMessages();
    }

    public void addDiseaseToSelectionBox(TermDTO disease) {
        if (diseaseList.contains(disease))
            return;
        diseaseList.add(disease);
        reCreateDiseaseListBox();
    }

    public void updateFishList() {
        // fish list

        String message = "Error while reading Fish";
        diseaseRpcService.getFishList(publicationID, new RetrieveFishListCallBack(message, view.getErrorLabel()));
    }

    public void updateConditions() {
        String message = "geting wildtype";
        curationRPCService.getBackgroundGenotypes(publicationID, new RetrieveBackgroundNewGenoCallback(message, view.getErrorLabel()));


       /* if (termSel.contains("WT")) {
            view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).setAttribute("disabled", "disabled");
            view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).setAttribute("disabled", "disabled");
            //view.getEnvironmentSelectionBox().setEnabled(false);
        }
        else{
            view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).removeAttribute("disabled");
            view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).removeAttribute("disabled");
        }*/
    }

    class RetrieveEnvironmentListCallBack extends ZfinAsyncCallback<List<EnvironmentDTO>> {

        public RetrieveEnvironmentListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<EnvironmentDTO> list) {
            view.getEnvironmentSelectionBox().clear();
            environmentList = list;
            view.getEnvironmentSelectionBox().addItem("None");
            for (EnvironmentDTO dto : list) {
                view.getEnvironmentSelectionBox().addItem(dto.getName(), dto.getZdbID());
            }

            resetUI();
        }
    }

    class RetrieveBackgroundNewGenoCallback extends ZfinAsyncCallback<List<GenotypeDTO>> {

        public RetrieveBackgroundNewGenoCallback(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<GenotypeDTO> list) {
            List<String> genos = new ArrayList<>();
            String termSel = view.getFishSelectionBox().getItemText(view.getFishSelectionBox().getSelectedIndex());

            for (GenotypeDTO dto : list) {
                genos.add(dto.getName());
            }
            genos.add("WT");
            if (view.getEnvironmentSelectionBox().getSelectedValue().equals("ZDB-EXP-041102-1") || view.getEnvironmentSelectionBox().getSelectedValue().equals("ZDB-EXP-070511-5")) {
                int selInd = view.getEnvironmentSelectionBox().getSelectedIndex();
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(selInd).setAttribute("disabled", "disabled");
            } else {
                int selInd = view.getEnvironmentSelectionBox().getSelectedIndex();
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(selInd).removeAttribute("disabled");
            }
            if (genos.contains(termSel)) {

                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).setAttribute("disabled", "disabled");
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).setAttribute("disabled", "disabled");
                //view.getEnvironmentSelectionBox().setEnabled(false);
            } else {
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(1).removeAttribute("disabled");
                view.getEnvironmentSelectionBox().getElement().getElementsByTagName("option").getItem(2).removeAttribute("disabled");
            }
        }
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            view.getFishSelectionBox().clear();
            fishList = list;
            view.getFishSelectionBox().addItem("None");
            for (FishDTO dto : list) {
                view.getFishSelectionBox().addItem(dto.getHandle(), dto.getZdbID());
            }
            resetUI();
        }
    }

    class RetrieveDiseaseModelListCallBack extends ZfinAsyncCallback<List<DiseaseAnnotationDTO>> {

        public RetrieveDiseaseModelListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.loadingImage);
        }

        @Override
        public void onSuccess(List<DiseaseAnnotationDTO> modelDTOs) {
            if (modelDTOs == null) {
                diseaseModelList.clear();
            } else {
                diseaseModelList = modelDTOs;
            }
            view.updateDiseaseModelTableContent(modelDTOs);
            updateDiseaseList(modelDTOs);
            view.getLoadingImage().setVisible(false);
            addDynamicClickHandler();
            processing = false;
        }

        @Override
        public void onFailure(Throwable throwable) {
            super.onFailure(throwable);
            processing = false;
        }
    }

    private class HumanDiseaseModelDeleteClickListener implements ClickHandler {

        private DiseaseAnnotationDTO diseaseAnnotationDTO;

        public HumanDiseaseModelDeleteClickListener(DiseaseAnnotationDTO diseaseAnnotationDTO) {
            this.diseaseAnnotationDTO = diseaseAnnotationDTO;
        }

        public void onClick(ClickEvent event) {
            diseaseRpcService.deleteDiseaseModel(diseaseAnnotationDTO, new RetrieveDiseaseModelListCallBack("Could not delete Disease model", view.getErrorLabel()));
        }
    }

    private class HumanDiseaseAnnotationModelDeleteClickListener implements ClickHandler {

        private DiseaseAnnotationModelDTO diseaseAnnotationModelDTO;

        public HumanDiseaseAnnotationModelDeleteClickListener(DiseaseAnnotationModelDTO diseaseAnnotationModelDTO) {
            this.diseaseAnnotationModelDTO = diseaseAnnotationModelDTO;
        }

        public void onClick(ClickEvent event) {
            diseaseRpcService.deleteDiseaseAnnotationModel(diseaseAnnotationModelDTO, new RetrieveDiseaseModelListCallBack("Could not delete Disease model", view.getErrorLabel()));
        }
    }

    private class PopulateTermEntryClickListener implements ClickHandler {

        private TermDTO termDTO;

        public PopulateTermEntryClickListener(TermDTO termDTO) {
            this.termDTO = termDTO;
        }

        public void onClick(ClickEvent event) {
            eventBus.fireEvent(new ClickTermEvent(termDTO));
        }
    }


}
