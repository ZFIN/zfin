package org.zfin.gwt.curation.ui;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Hyperlink;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.ErrorHandler;
import org.zfin.gwt.root.ui.ZfinAsyncCallback;

import java.util.*;

/**
 * Table of associated genotypes
 */
public class DiseaseModelPresenter implements Presenter {

    private CurationDiseaseRPCAsync diseaseRpcService = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationRPCService = CurationExperimentRPC.App.getInstance();
    private final HandlerManager eventBus;
    private DiseaseModelView view;
    private String publicationID;

    public DiseaseModelPresenter(HandlerManager eventBus, DiseaseModelView view, String publicationID) {
        this.eventBus = eventBus;
        this.view = view;
        this.publicationID = publicationID;
    }

    private List<FishDTO> fishList = new ArrayList<>();
    private List<EnvironmentDTO> environmentList = new ArrayList<>();
    private List<TermDTO> diseaseList = new ArrayList<>();
    private List<DiseaseModelDTO> diseaseModelList = new ArrayList<>();

    public void bind() {
        view.getFishSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        view.getEnvironmentSelectionBox().addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        view.getAddDiseaseModelButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if (processing)
                    return;
                processing = true;
                DiseaseModelDTO disease = getDiseaseModel();
                if (disease == null) {
                    processing = false;
                    return;
                }
                diseaseRpcService.addHumanDiseaseModel(disease, new RetrieveDiseaseModelListCallBack("Could not add a new disease model", view.getErrorLabel()));
                view.getLoadingImage().setVisible(true);
            }
        });
        addDynamicClickHandler();
    }

    private boolean processing = false;


    private DiseaseModelDTO getDiseaseModel() {
        DiseaseModelDTO dto = new DiseaseModelDTO();
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
        Map<Button, DiseaseModelDTO> map = view.getDeleteModeMap();
        for (Button deleteButton : map.keySet()) {
            deleteButton.addClickHandler(new HumanDiseaseModelDeleteClickListener(map.get(deleteButton)));
        }

        Map<Hyperlink, DiseaseModelDTO> linkMap = view.getTermLinkDiseaseModelMap();
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


    private void updateDiseaseList(List<TermDTO> termList) {
        diseaseList = new ArrayList<>(termList);
        Collections.sort(diseaseList);
        view.getDiseaseSelectionBox().clear();
        for (TermDTO disease : diseaseList)
            view.getDiseaseSelectionBox().addItem(disease.getTermName(), disease.getZdbID());
    }

    public void setError(String message) {
        view.getErrorLabel().setText(message);
    }

    /*

        public void addError(String message) {
            String mess = errorLabel.getText();
            if (StringUtils.isEmpty(mess))
                mess = message;
            else
                mess += " || " + message;
            errorLabel.setText(mess);
        }

        @Override
        public void clearError() {
            attributionModule.clearError();
            revertGUI();
        }

    */
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
        List<TermDTO> diseaseList = new ArrayList<>(this.diseaseList.size() + 1);
        diseaseList.add(disease);
        diseaseList.addAll(this.diseaseList);
        this.diseaseList = diseaseList;
        view.getDiseaseSelectionBox().insertItem(disease.getTermName(), disease.getZdbID(), 0);
        view.getDiseaseSelectionBox().setSelectedIndex(0);
    }

    public void updateFishList() {
        // fish list
        String message = "Error while reading Fish";
        diseaseRpcService.getFishList(publicationID, new RetrieveFishListCallBack(message, view.getErrorLabel()));
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

    class RetrieveDiseaseModelListCallBack extends ZfinAsyncCallback<List<DiseaseModelDTO>> {

        public RetrieveDiseaseModelListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, view.loadingImage);
        }

        @Override
        public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
            if (modelDTOs == null) {
                diseaseModelList.clear();
            } else {
                diseaseModelList = modelDTOs;
                Set<TermDTO> diseaseList = new HashSet<>(modelDTOs.size());
                for (DiseaseModelDTO dto : modelDTOs) {
                    diseaseList.add(dto.getDisease());
                }
            }
            view.updateDiseaseModelTableContent(modelDTOs);
            updateDiseaseList(diseaseList);
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

        private DiseaseModelDTO diseaseModelDTO;

        public HumanDiseaseModelDeleteClickListener(DiseaseModelDTO diseaseModelDTO) {
            this.diseaseModelDTO = diseaseModelDTO;
        }

        public void onClick(ClickEvent event) {
            diseaseRpcService.deleteDiseaseModel(diseaseModelDTO, new RetrieveDiseaseModelListCallBack("Could not delete Disease model", view.getErrorLabel()));
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
