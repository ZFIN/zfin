package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.DiseaseModelDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class DiseaseModelView extends Composite {

    public static final String IS_A_MODEL_OF = "is a model of";

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);

    @UiTemplate("DiseaseModule.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, DiseaseModelView> {
    }

    public DiseaseModelView() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    Image loadingImage;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    ZfinFlexTable diseaseModelTable;


    private ListBox fishSelectionBox = new ListBox();
    private ListBox environmentSelectionBox = new ListBox();
    private ListBox diseaseSelectionBox = new ListBox();
    private ListBox evidenceCodeSelectionBox = new ListBox();
    private Button addDiseaseModelButton = new Button("Add");

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
        diseaseModelTable.getCellFormatter().setStyleName(0, index, "bold");
        diseaseModelTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_LEFT);
        diseaseModelTable.getRowFormatter().setStyleName(0, "table-header");
    }

    private Map<Button, DiseaseModelDTO> deleteModeMap = new HashMap<>();
    private Map<Hyperlink, DiseaseModelDTO> termLinkDiseaseModelMap = new HashMap<>();

    public void updateDiseaseModelTableContent(List<DiseaseModelDTO> modelDTOList) {
        diseaseModelTable.removeAllRows();
        initDiseaseModelTable();
        int groupIndex = 0;
        int rowIndex = 1;
        if (modelDTOList != null) {
            for (DiseaseModelDTO diseaseModel : modelDTOList) {
                int colIndex = 0;
                if (diseaseModel.getFish() != null) {
                    Anchor fish = new Anchor(SafeHtmlUtils.fromTrustedString(diseaseModel.getFish().getHandle()), "/" + diseaseModel.getFish().getZdbID());
                    fish.setTitle(diseaseModel.getFish().getZdbID());
                    diseaseModelTable.setWidget(rowIndex, colIndex++, fish);
                    InlineHTML environment = new InlineHTML(diseaseModel.getEnvironment().getName());
                    environment.setTitle(diseaseModel.getEnvironment().getZdbID());
                    diseaseModelTable.setWidget(rowIndex, colIndex++, environment);
                } else {
                    colIndex += 2;
                }
                diseaseModelTable.setText(rowIndex, colIndex, IS_A_MODEL_OF);
                diseaseModelTable.getCellFormatter().setStyleName(rowIndex, colIndex++, "bold");
                Hyperlink disease = new Hyperlink(SafeHtmlUtils.fromTrustedString(diseaseModel.getDisease().getTermName()), "diseaseName");
                disease.setTitle(diseaseModel.getDisease().getOboID());
                termLinkDiseaseModelMap.put(disease, diseaseModel);
                diseaseModelTable.setWidget(rowIndex, colIndex++, disease);
                diseaseModelTable.setText(rowIndex, colIndex++, diseaseModel.getEvidenceCode());
                Button deleteButton = new Button("X");
                deleteModeMap.put(deleteButton, diseaseModel);
                deleteButton.setTitle("ID: " + diseaseModel.getID());
                diseaseModelTable.setWidget(rowIndex, colIndex, deleteButton);
                groupIndex = diseaseModelTable.setRowStyle(rowIndex++, null, diseaseModel.getDisease().getZdbID(), groupIndex);
            }
        }
        addConstructionRow(rowIndex);
    }

    private void addConstructionRow(int rowIndex) {
        int colIndex = 0;
        diseaseModelTable.setWidget(rowIndex, colIndex++, fishSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex++, environmentSelectionBox);
        diseaseModelTable.setText(rowIndex, colIndex++, IS_A_MODEL_OF);
        diseaseModelTable.setWidget(rowIndex, colIndex++, diseaseSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex++, evidenceCodeSelectionBox);
        diseaseModelTable.setWidget(rowIndex, colIndex, addDiseaseModelButton);
        diseaseModelTable.getRowFormatter().setStyleName(rowIndex, "table-header");
    }


    public Button getAddDiseaseModelButton() {
        return addDiseaseModelButton;
    }

    public ListBox getDiseaseSelectionBox() {
        return diseaseSelectionBox;
    }

    public ListBox getEnvironmentSelectionBox() {
        return environmentSelectionBox;
    }

    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }

    public ListBox getEvidenceCodeSelectionBox() {
        return evidenceCodeSelectionBox;
    }

    public ListBox getFishSelectionBox() {
        return fishSelectionBox;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public Map<Button, DiseaseModelDTO> getDeleteModeMap() {
        return deleteModeMap;
    }

    public Map<Hyperlink, DiseaseModelDTO> getTermLinkDiseaseModelMap() {
        return termLinkDiseaseModelMap;
    }
}
