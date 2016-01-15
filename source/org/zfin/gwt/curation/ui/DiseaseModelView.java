package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.ZfinFlexTable;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table of associated genotypes
 */
public class DiseaseModelView extends Composite {

    public static final String IS_A_MODEL_OF = "is a model of";

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private DiseaseModelPresenter presenter;

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
    @UiField
    Grid dataTable;
    @UiField
    ListBox fishSelectionBox;
    @UiField
    ListBox environmentSelectionBox;
    @UiField
    ListBox diseaseSelectionBox;
    @UiField
    ListBox evidenceCodeSelectionBox;
    @UiField
    Button addDiseaseModelButton;


    @UiHandler("addDiseaseModelButton")
    void onAddModel(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addModelEvent();
    }

    private Map<DeleteImage, DiseaseAnnotationDTO> deleteModeMap = new HashMap<>();
    private Map<DeleteImage, DiseaseAnnotationModelDTO> deleteModeMap1 = new HashMap<>();
    private Map<Hyperlink, DiseaseAnnotationDTO> termLinkDiseaseModelMap = new HashMap<>();

    protected void updateDiseaseModelTableContent(List<DiseaseAnnotationDTO> modelDTOList) {
        if (modelDTOList == null)
            return;
        int rows = getNumberOfRows(modelDTOList);
//        com.google.gwt.user.client.Window.alert("hi " + row);
        dataTable.resize(rows + 2, 6);
        // leave the header table row unchanged
        int row = 1;
        // responsible for the alternate shading
        int groupIndex = 0;
        for (DiseaseAnnotationDTO disease : modelDTOList) {
            if (disease.getDamoDTO() != null) {
                for (DiseaseAnnotationModelDTO dto : disease.getDamoDTO()) {
                    groupIndex = populateRow(row, disease, dto, groupIndex);
                    row++;
                }
            } else {
                // ensure the old values in these cells are not retained from previous display...
                dataTable.clearCell(row, 0);
                dataTable.clearCell(row, 1);
                dataTable.clearCell(row, 2);
                groupIndex = populateRow(row, disease, null, groupIndex);
                row++;
            }
        }
        int lastRow = rows + 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, fishSelectionBox);
        dataTable.setWidget(lastRow, col++, environmentSelectionBox);
        dataTable.setText(lastRow, col, IS_A_MODEL_OF);
        dataTable.getCellFormatter().setStyleName(row, col++, "bold");
        dataTable.setWidget(lastRow, col++, diseaseSelectionBox);
        dataTable.setWidget(lastRow, col++, evidenceCodeSelectionBox);
        dataTable.getRowFormatter().setStyleName(lastRow, "table-header");
        dataTable.setWidget(lastRow, col, addDiseaseModelButton);
    }

    // returns new groupIndex
    private int populateRow(int row, DiseaseAnnotationDTO disease, DiseaseAnnotationModelDTO dto, int groupIndex) {
        int column = 0;
        if (dto != null) {
            Anchor fish = new Anchor(SafeHtmlUtils.fromTrustedString(dto.getFish().getHandle()), "/" + dto.getFish().getZdbID());
            fish.setTitle(dto.getFish().getZdbID());
            dataTable.setWidget(row, column++, fish);
            dataTable.setText(row, column++, dto.getEnvironment().getName());
            dataTable.setText(row, column, IS_A_MODEL_OF);
            dataTable.getCellFormatter().setStyleName(row, column++, "bold");
        } else {
            column += 3;
        }
        Hyperlink diseaseHyperlink = new Hyperlink(SafeHtmlUtils.fromTrustedString(disease.getDisease().getTermName()), "diseaseName");
        diseaseHyperlink.setTitle(disease.getDisease().getOboID());
        termLinkDiseaseModelMap.put(diseaseHyperlink, disease);
        dataTable.setWidget(row, column++, diseaseHyperlink);
        dataTable.setText(row, column++, disease.getEvidenceCode());
        DeleteImage deleteImage;
        if (dto == null) {
            deleteImage = new DeleteImage("Delete Disease Annotation");
            deleteModeMap.put(deleteImage, disease);
        } else {
            deleteImage = new DeleteImage("Delete Fish Model from Annotation");
            deleteModeMap1.put(deleteImage, dto);
        }
        deleteImage.setTitle(deleteImage.getTitle() + ": " + disease.getZdbID());
        dataTable.setWidget(row, column, deleteImage);
        return WidgetUtil.setRowStyle(row, null, disease.getDisease().getZdbID(), groupIndex, dataTable);
    }

    private int getNumberOfRows(List<DiseaseAnnotationDTO> modelDTOList) {
        int rows = 0;
        for (DiseaseAnnotationDTO diseaseModel : modelDTOList) {
            if (diseaseModel.getDamoDTO() != null)
                rows += diseaseModel.getDamoDTO().size();
            else
                rows += 1;
        }
        return rows;
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

    public Map<DeleteImage, DiseaseAnnotationDTO> getDeleteModeMap() {
        return deleteModeMap;
    }

    public Map<DeleteImage, DiseaseAnnotationModelDTO> getDeleteModeMap1() {
        return deleteModeMap1;
    }

    public Map<Hyperlink, DiseaseAnnotationDTO> getTermLinkDiseaseModelMap() {
        return termLinkDiseaseModelMap;
    }

    public void setPresenter(DiseaseModelPresenter presenter) {
        this.presenter = presenter;
    }

    public DiseaseModelPresenter getPresenter() {
        return presenter;
    }

}
