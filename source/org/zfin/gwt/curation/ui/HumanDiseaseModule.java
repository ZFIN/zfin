package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.dto.RelatedEntityDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for FX curation module.
 */
public class HumanDiseaseModule implements HandlesError, EntryPoint {

    public static final String IS_A_MODEL_OF = "is a model of";
    public static final String HUMAN_DISEASE_ZONE = "humanDiseaseZone";
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("HumanDiseaseModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, HumanDiseaseModule> {
    }

    // data
    private String publicationID;

    private AttributionModule attributionModule;

    // listener
    private List<HandlesError> handlesErrorListeners = new ArrayList<>();

    @UiField
    FlexTable diseaseTable;

    @UiField
    Label diseaseTableContent;

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
                        updateDiseaseTableContent(termDTOs);
                        resetUI();
                    }

                });
            }
        });
        // hide this section until we have fish IDs
        diseaseModelTable.setVisible(false);
        attributionModule = new AttributionModule();
        RelatedEntityDTO relatedEntityDTO = new RelatedEntityDTO();
        relatedEntityDTO.setPublicationZdbID(publicationID);
        attributionModule.setDTO(relatedEntityDTO);
    }

    private void resetUI() {
        termEntry.getTermTextBox().setText("");
        diseaseErrorLabel.clearAllErrors();
    }

    private CurationDiseaseRPCAsync diseaseCurationRPCAsync = CurationDiseaseRPC.App.getInstance();
    private CurationExperimentRPCAsync curationExperimentRPCAsync = CurationExperimentRPC.App.getInstance();

    private ListBox fishList = new ListBox();
    private ListBox environmentList = new ListBox();
    private ListBox diseaseList = new ListBox();

    private void retrieveAllValues() {
        // human disease list
        diseaseCurationRPCAsync.getHumanDiseaseList(publicationID, new ZfinAsyncCallback<List<TermDTO>>(null, null) {

            @Override
            public void onSuccess(List<TermDTO> termDTOs) {
                updateDiseaseTableContent(termDTOs);
            }
        });

        // human disease model list
/*
        diseaseCurationRPCAsync.getHumanDiseaseModelList(publicationID, new ZfinAsyncCallback<List<DiseaseModelDTO>>(null, null) {

            @Override
            public void onSuccess(List<DiseaseModelDTO> termDTOs) {
                updateDiseaseModelTableContent(termDTOs);
            }
        });

        // fish (genotype) list
        String message = "Error while reading Genotypes";
        curationExperimentRPCAsync.getGenotypes(publicationID,
                new RetrieveGenotypeListCallBack(fishList, message, null));

        // environment list
        message = "Error while reading the environment";
        curationExperimentRPCAsync.getEnvironments(publicationID,
                new RetrieveEnvironmentListCallBack(environmentList, message, null));

*/
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
        diseaseModelTable.setText(0, index, "");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Genotype");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Environment");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index++, "");
        diseaseModelTable.setText(0, index, "Human Disease");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.setText(0, index, "Delete");
        diseaseModelTable.getCellFormatter().setStyleName(0, index++, "bold");
        diseaseModelTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_LEFT);
        diseaseModelTable.getRowFormatter().setStyleName(0, "table-header");
    }

    public void updateDiseaseTableContent(List<TermDTO> termList) {
        int index = 1;
        diseaseTable.removeAllRows();
        if (termList == null || termList.size() == 0) {
            diseaseTableContent.setVisible(true);
            return;
        }

        diseaseTableContent.setVisible(false);
        initReportedDiseaseTable();
        diseaseList.clear();

        for (TermDTO term : termList) {
            diseaseTable.setText(index, 0, term.getTermName());
            Button deleteButton = new Button("X");
            deleteButton.addClickHandler(new HumanDiseaseDeleteClickListener(term));
            diseaseTable.setWidget(index++, 1, deleteButton);
            diseaseList.addItem(term.getTermName(), term.getZdbID());
        }
    }

    public void updateDiseaseModelTableContent(List<DiseaseModelDTO> modelDTOList) {
        diseaseTable.removeAllRows();
        if (modelDTOList == null || modelDTOList.size() == 0)
            diseaseModelTableContent.setVisible(true);
        else
            diseaseModelTableContent.setVisible(false);

        initDiseaseModelTable();
        int index = 1;
        if (modelDTOList != null) {
            int colIndex = 1;
            for (DiseaseModelDTO term : modelDTOList) {
                diseaseModelTable.setText(index, colIndex++, term.getGenotype().getHandle());
                diseaseModelTable.setText(index, colIndex++, term.getEnvironment().getName());
                diseaseModelTable.setText(index, colIndex++, IS_A_MODEL_OF);
                diseaseModelTable.setText(index, colIndex++, term.getTerm().getTermName());
                Button deleteButton = new Button("X");
                deleteButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        Window.alert("Delete");
                    }
                });
                diseaseModelTable.setWidget(index++, colIndex++, deleteButton);
            }
        }
        addConstructionRow(index);
    }

    private Button addDiseaseModelButton = new Button("Add");

    private void addConstructionRow(int index) {
        int colIndex = 0;
        addDiseaseModelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                Window.alert("Click new disease");
                diseaseCurationRPCAsync.addHumanDiseaseModel(null, publicationID, new ZfinAsyncCallback<List<DiseaseModelDTO>>("Could not add a new disease model", null) {
                    @Override
                    public void onSuccess(List<DiseaseModelDTO> modelDTOs) {
                        Window.alert("Added new model");
                        updateDiseaseModelTableContent(modelDTOs);
                    }

                });
            }
        });
        diseaseModelTable.setWidget(index, colIndex++, addDiseaseModelButton);
        diseaseModelTable.setWidget(index, colIndex++, fishList);
        diseaseModelTable.setWidget(index, colIndex++, environmentList);
        diseaseModelTable.setText(index, colIndex++, "is a model of");
        diseaseModelTable.setWidget(index, colIndex++, diseaseList);
    }

    @Override
    public void setError(String message) {
    }

    @Override
    public void clearError() {
        attributionModule.clearError();
        revertGUI();
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
            diseaseCurationRPCAsync.deleteHumanDisease(term, publicationID, new ZfinAsyncCallback<List<TermDTO>>("Could not delete Human Disease", null) {

                @Override
                public void onSuccess(List<TermDTO> termDTOs) {
                    updateDiseaseTableContent(termDTOs);
                }
            });
        }

    }

}
