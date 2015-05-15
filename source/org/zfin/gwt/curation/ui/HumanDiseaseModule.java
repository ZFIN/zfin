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

import java.util.ArrayList;
import java.util.List;

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
    ZfinFlexTable diseaseTable;
    @UiField
    Label diseaseTableContent;
    @UiField
    Image loadingImageDiseaseModels;
    @UiField
    Image loadingImageDiseases;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Label diseaseModelTableContent;
    @UiField
    FlexTable diseaseModelTable;
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
                TermDTO term = termInfoBox.getCurrentTermInfoDTO();
                diseaseCurationRPCAsync.saveHumanDisease(term, publicationID, new ZfinAsyncCallback<List<TermDTO>>("Could not add a disease", diseaseErrorLabel) {
                    @Override
                    public void onSuccess(List<TermDTO> termDTOs) {
                        diseaseList = termDTOs;
                        updateDiseaseTableContent(termDTOs);
                        resetUI();
                    }

                });
            }
        });
        // hide this section until we have fish IDs
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
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
        // human disease list
        diseaseCurationRPCAsync.getHumanDiseaseList(publicationID, new ZfinAsyncCallback<List<TermDTO>>(null, diseaseErrorLabel) {
            @Override
            public void onSuccess(List<TermDTO> termDTOs) {
                diseaseList = termDTOs;
                updateDiseaseTableContent(termDTOs);
            }
        });

        // human disease model list
        diseaseCurationRPCAsync.getHumanDiseaseModelList(publicationID, new ZfinAsyncCallback<List<DiseaseModelDTO>>(null, diseaseErrorLabel) {

            @Override
            public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
                diseaseModelList = modelDTOs;
                updateDiseaseModelTableContent(modelDTOs);
            }
        });

        // fish (genotype) list
        String message = "Error while reading Genotypes";
        diseaseCurationRPCAsync.getFishList(publicationID, new RetrieveFishListCallBack("Fish List", diseaseErrorLabel));

        // environment list
        message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironments(publicationID, new RetrieveEnvironmentListCallBack("Environment", diseaseErrorLabel));

    }


    private void initReportedDiseaseTable() {
        // Initialize the diseaseTable.
        diseaseTable.setText(0, 0, "Human Disease");
        diseaseTable.getCellFormatter().setStyleName(0, 0, "bold");
        diseaseTable.setText(0, 1, "Delete");
        diseaseTable.getCellFormatter().setStyleName(0, 1, "bold");
        diseaseTable.getRowFormatter().setStyleName(0, "table-header");

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

    /**
     * Re-creates the disease table and
     * the disease selection box in the disease model construction zone
     *
     * @param termList
     */
    public void updateDiseaseTableContent(List<TermDTO> termList) {
        int index = 1;
        diseaseTable.removeAllRows();
        diseaseSelectionBox.clear();
        if (termList == null || termList.size() == 0) {
            diseaseTableContent.setVisible(true);
            loadingImageDiseases.setVisible(false);
            return;
        }

        diseaseTableContent.setVisible(false);
        initReportedDiseaseTable();

        int groupIndex = 1;
        int rowIndex = 1;
        for (TermDTO term : termList) {
            diseaseTable.setText(index, 0, term.getTermName());
            Button deleteButton = new Button("X");
            deleteButton.addClickHandler(new HumanDiseaseDeleteClickListener(term));
            diseaseTable.setWidget(index++, 1, deleteButton);
            diseaseSelectionBox.addItem(term.getTermName(), term.getZdbID());
            groupIndex = diseaseTable.setRowStyle(rowIndex++, null, term.getZdbID(), groupIndex);

        }
    }

    public void updateDiseaseModelTableContent(List<DiseaseModelDTO> modelDTOList) {
        diseaseModelTable.removeAllRows();
        if (modelDTOList == null || modelDTOList.size() == 0)
            diseaseModelTableContent.setVisible(false);
        else
            diseaseModelTableContent.setVisible(true);

        initDiseaseModelTable();
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
                diseaseModelTable.setWidget(rowIndex++, colIndex++, deleteButton);
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
        int selectedIndex = fishSelectionBox.getSelectedIndex();
        if (selectedIndex == -1) {
            setError("No Fish available. Please create a new Fish on the Fish tab first.");
            return null;
        }
        dto.setFish(fishList.get(selectedIndex));
        dto.setEnvironment(environmentList.get(environmentSelectionBox.getSelectedIndex()));
        selectedIndex = diseaseSelectionBox.getSelectedIndex();
        if (selectedIndex == -1) {
            setError("No Disease available. Please add a new disease below.");
            return null;
        }
        dto.setDisease(diseaseList.get(selectedIndex));
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

    private class HumanDiseaseDeleteClickListener implements ClickHandler {

        private TermDTO term;

        public HumanDiseaseDeleteClickListener(TermDTO term) {
            this.term = term;
        }

        public void onClick(ClickEvent event) {
            if (diseaseUsedInModel(term)) {
                setError("Cannot delete " + term.getName() + " as it is used in at least one model");
                return;
            }
            diseaseCurationRPCAsync.deleteHumanDisease(term, publicationID, new ZfinAsyncCallback<List<TermDTO>>("Could not delete Human Disease", null) {

                @Override
                public void onSuccess(List<TermDTO> termDTOs) {
                    diseaseList = termDTOs;
                    updateDiseaseTableContent(termDTOs);
                }
            });
        }

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

    private boolean diseaseUsedInModel(TermDTO term) {
        for (DiseaseModelDTO model : diseaseModelList) {
            if (model.getDisease().equals(term))
                return true;
        }
        return false;
    }

    class RetrieveFishListCallBack extends ZfinAsyncCallback<List<FishDTO>> {

        public RetrieveFishListCallBack(String errorMessage, ErrorHandler errorLabel) {
            super(errorMessage, errorLabel, loadingImageDiseases);
        }

        @Override
        public void onSuccess(List<FishDTO> list) {
            fishSelectionBox.clear();
            fishList = list;
            for (FishDTO dto : list) {
                fishSelectionBox.addItem(dto.getName(), dto.getZdbID());
            }
            loadingImageDiseases.setVisible(false);
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
            for (EnvironmentDTO dto : list) {
                environmentSelectionBox.addItem(dto.getName(), dto.getZdbID());
            }
            resetUI();
        }
    }

}
