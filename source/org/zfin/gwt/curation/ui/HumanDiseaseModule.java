package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.StringUtils;

import java.util.*;

/**
 * Entry point for FX curation module.
 */
public class HumanDiseaseModule implements HandlesError, EntryPoint {

    public static final String IS_A_MODEL_OF = "is a model of";
    public static final String HUMAN_DISEASE_ZONE = "humanDiseaseZone";
    public static final String SELECT = "Select";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private CurationDiseaseRPCAsync diseaseCurationRPCAsync = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    private ListBox fishSelectionBox = new ListBox();
    private List<FishDTO> fishList = new ArrayList<>();
    private List<EnvironmentDTO> environmentList = new ArrayList<>();
    private List<TermDTO> diseaseList = new ArrayList<>();
    private List<DiseaseModelDTO> diseaseModelList = new ArrayList<>();
    private ListBox environmentSelectionBox = new ListBox();
    private ListBox diseaseSelectionBox = new ListBox();
    private ListBox evidenceCodeSelectionBox = new ListBox();

    RetrieveRelatedEntityListCallBack environmentCallBack;

    @UiTemplate("HumanDiseaseModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, HumanDiseaseModule> {
    }

    // data
    private String publicationID;

    private AttributionModule attributionModule;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    Image loadingImageDiseaseModels;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Label diseaseModelTableContent;
    @UiField
    ZfinFlexTable diseaseModelTable;
    @UiField
    TermInfoComposite termInfoBox;
    @UiField
    TermEntry termEntry;
    @UiField
    Button addButton;
    @UiField
    Button resetButton;
    @UiField
    SimpleErrorElement diseaseErrorLabel;

    public HumanDiseaseModule(String publicationID) {
        this.publicationID = publicationID;
        onModuleLoad();
    }

    @Override
    public void onModuleLoad() {

        FlowPanel outer = uiBinder.createAndBindUi(this);
        termEntry.getTermTextBox().setTermInfoTable(termInfoBox);
        termEntry.setTermInfoTable(termInfoBox);
        environmentCallBack = new RetrieveRelatedEntityListCallBack(environmentSelectionBox, "Environment", null);

        initDiseaseModelTable();
        setEvidenceCode();
        retrieveAllValues();
        RootPanel.get(HUMAN_DISEASE_ZONE).add(outer);
        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                termEntry.reset();
                diseaseErrorLabel.clearError();
            }
        });
        addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                TermDTO disease = termInfoBox.getCurrentTermInfoDTO();
                addDiseaseToSelectionBox(disease);
            }
        });
        fishSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        environmentSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        // hide this section until we have fish IDs
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
    }

    private void addDiseaseToSelectionBox(TermDTO disease) {
        if (diseaseList.contains(disease))
            return;
        List<TermDTO> diseaseList = new ArrayList<>(this.diseaseList.size() + 1);
        diseaseList.add(disease);
        diseaseList.addAll(this.diseaseList);
        this.diseaseList = diseaseList;
        diseaseSelectionBox.insertItem(disease.getTermName(), disease.getZdbID(), 0);
        diseaseSelectionBox.setSelectedIndex(0);
    }

    private void setEvidenceCode() {
        evidenceCodeSelectionBox.addItem(SELECT);
        evidenceCodeSelectionBox.addItem("TAS");
        evidenceCodeSelectionBox.addItem("IC");
        evidenceCodeSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
        evidenceCodeSelectionBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent changeEvent) {
                clearErrorMessages();
            }
        });
    }

    private void resetUI() {
        termEntry.getTermTextBox().setText("");
        diseaseErrorLabel.clearAllErrors();
    }


    private void retrieveAllValues() {

        // human disease model list
        diseaseCurationRPCAsync.getHumanDiseaseModelList(publicationID, new ZfinAsyncCallback<List<DiseaseModelDTO>>(null, diseaseErrorLabel) {

            @Override
            public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
                if (modelDTOs == null) {
                    return;
                }
                diseaseModelList = modelDTOs;
                updateDiseaseModelTableContent(modelDTOs);
                Set<TermDTO> diseaseList = new HashSet<>(modelDTOs.size());
                for (DiseaseModelDTO dto : modelDTOs) {
                    diseaseList.add(dto.getDisease());
                }
                updateDiseaseList(diseaseList);
            }
        });

        // fish (genotype) list
        String message = "Error while reading Genotypes";
        diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", diseaseErrorLabel));

        // environment list
        message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironmentsWithoutSTR(publicationID, new RetrieveEnvironmentListCallBack("Environment", diseaseErrorLabel));

    }

    private void updateDiseaseList(Set<TermDTO> termList) {
        diseaseList = new ArrayList<>(termList);
        Collections.sort(diseaseList);
        diseaseSelectionBox.clear();
        for (TermDTO disease : diseaseList)
            diseaseSelectionBox.addItem(disease.getTermName(), disease.getZdbID());
    }


    private void initDiseaseModelTable() {
        // Initialize the diseaseTable.
        int index = 0;
        diseaseModelTable.setText(0, index, "Fish");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Environment");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index++, "");
        diseaseModelTable.setText(0, index, "Human Disease");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Evidence Code");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Delete");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_LEFT);
        diseaseModelTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void updateDiseaseModelTableContent(List<DiseaseModelDTO> modelDTOList) {
        diseaseModelTable.removeAllRows();
        if (modelDTOList == null || modelDTOList.size() == 0)
            diseaseModelTableContent.setVisible(true);
        else
            diseaseModelTableContent.setVisible(false);

        initDiseaseModelTable();
        int groupIndex = 0;
        int rowIndex = 1;
        if (modelDTOList != null) {
            for (DiseaseModelDTO diseaseModel : modelDTOList) {
                int colIndex = 0;
                InlineHTML fish = new InlineHTML(diseaseModel.getFish().getHandle());
                fish.setTitle(diseaseModel.getFish().getZdbID());
                diseaseModelTable.setWidget(rowIndex, colIndex++, fish);
                InlineHTML environment = new InlineHTML(diseaseModel.getEnvironment().getName());
                environment.setTitle(diseaseModel.getEnvironment().getZdbID());
                diseaseModelTable.setWidget(rowIndex, colIndex++, environment);
                diseaseModelTable.setText(rowIndex, colIndex, IS_A_MODEL_OF);
                diseaseModelTable.getCellFormatter().setStyleName(rowIndex, colIndex++, "bold");
                InlineHTML disease = new InlineHTML(diseaseModel.getDisease().getTermName());
                disease.setTitle(diseaseModel.getDisease().getZdbID());
                diseaseModelTable.setWidget(rowIndex, colIndex++, disease);
                diseaseModelTable.setText(rowIndex, colIndex++, diseaseModel.getEvidenceCode());
                Button deleteButton = new Button("X");
                deleteButton.addClickHandler(new HumanDiseaseModelDeleteClickListener(diseaseModel));
                deleteButton.setTitle("ID: " + diseaseModel.getID());
                diseaseModelTable.setWidget(rowIndex, colIndex++, deleteButton);
                groupIndex = diseaseModelTable.setRowStyle(rowIndex++, null, diseaseModel.getDisease().getZdbID(), groupIndex);
            }
        }
        addConstructionRow(rowIndex);
        loadingImageDiseaseModels.setVisible(false);
    }

    private Button addDiseaseModelButton = new Button("Add");

    private void addConstructionRow(int rowIndex) {
        int colIndex = 0;
        addDiseaseModelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                DiseaseModelDTO disease = getDiseaseModel();
                if (disease == null) {
                    return;
                }
                diseaseCurationRPCAsync.addHumanDiseaseModel(disease, new ZfinAsyncCallback<List<DiseaseModelDTO>>("Could not add a new disease model", errorLabel) {
                    @Override
                    public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
                        diseaseModelList = modelDTOs;
                        updateDiseaseModelTableContent(modelDTOs);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        super.onFailure(throwable);
                        loadingImageDiseaseModels.setVisible(false);
                    }
                });
                loadingImageDiseaseModels.setVisible(true);
            }
        });
        diseaseModelTable.setWidget(rowIndex, colIndex++, fishSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex++, environmentSelectionBox);
        diseaseModelTable.setText(rowIndex, colIndex++, IS_A_MODEL_OF);
        diseaseModelTable.setWidget(rowIndex, colIndex++, diseaseSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex++, evidenceCodeSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex++, addDiseaseModelButton);
        diseaseModelTable.getRowFormatter().setStyleName(rowIndex, "table-header");
    }

    private DiseaseModelDTO getDiseaseModel() {
        DiseaseModelDTO dto = new DiseaseModelDTO();
        int selectedIndexFish = fishSelectionBox.getSelectedIndex();
        int selectedIndexEnv = environmentSelectionBox.getSelectedIndex();
        if ((selectedIndexFish == 0 && selectedIndexEnv > 0) ||
                (selectedIndexFish > 0 && selectedIndexEnv == 0)) {
            setError("You need to select both a Fish and an Environment or none at all.");
            return null;
        }
        if (selectedIndexEnv > 0 && selectedIndexFish > 0) {
            dto.setFish(fishList.get(selectedIndexFish - 1));
            dto.setEnvironment(environmentList.get(selectedIndexEnv - 1));
        }
        int selectedIndexDis = diseaseSelectionBox.getSelectedIndex();
        if (selectedIndexDis == -1) {
            setError("No Disease available. Please add a new disease below.");
            return null;
        }
        dto.setDisease(diseaseList.get(selectedIndexDis));
        String itemText = evidenceCodeSelectionBox.getItemText(evidenceCodeSelectionBox.getSelectedIndex());
        if (itemText.equals(SELECT)) {
            setError("Please select a valid Evidence Code");
            return null;
        }
        dto.setEvidenceCode(itemText);
        PublicationDTO pubDto = new PublicationDTO();
        pubDto.setZdbID(publicationID);
        dto.setPublication(pubDto);
        return dto;

    }

    @Override
    public void setError(String message) {
        errorLabel.setText(message);
    }

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

    public void clearErrorMessages() {
        errorLabel.setError("");
    }

    private void revertGUI() {
        attributionModule.revertGUI();
    }

    @Override
    public void fireEventSuccess() {
        for (HandlesError handlesError : handlesErrorListeners) {
            handlesError.clearError();
        }
    }

    @Override
    public void addHandlesErrorListener(HandlesError handlesError) {
        handlesErrorListeners.add(handlesError);
    }

    private class HumanDiseaseModelDeleteClickListener implements ClickHandler {

        private DiseaseModelDTO diseaseModelDTO;

        public HumanDiseaseModelDeleteClickListener(DiseaseModelDTO diseaseModelDTO) {
            this.diseaseModelDTO = diseaseModelDTO;
        }

        public void onClick(ClickEvent event) {
            diseaseCurationRPCAsync.deleteDiseaseModel(diseaseModelDTO, new ZfinAsyncCallback<List<DiseaseModelDTO>>("Could not delete Human Disease Model", null) {

                @Override
                public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
                    diseaseModelList = modelDTOs;
                    updateDiseaseModelTableContent(modelDTOs);
                }
            });
        }
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            fishSelectionBox.clear();
            fishList = list;
            fishSelectionBox.addItem("Select" +
                    "");
            for (FishDTO dto : list) {
                fishSelectionBox.addItem(dto.getName(), dto.getZdbID());
            }
            resetUI();
        }
    }

    class RetrieveEnvironmentListCallBack extends ZfinAsyncCallback<List<EnvironmentDTO>> {

        public RetrieveEnvironmentListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel);
        }

        @Override
        public void onSuccess(List<EnvironmentDTO> list) {
            environmentSelectionBox.clear();
            environmentList = list;
            environmentSelectionBox.addItem("Select");
            for (EnvironmentDTO dto : list) {
                environmentSelectionBox.addItem(dto.getName(), dto.getZdbID());
            }
            resetUI();
        }
    }

}
