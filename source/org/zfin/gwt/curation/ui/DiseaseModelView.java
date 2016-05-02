package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.curation.dto.DiseaseAnnotationDTO;
import org.zfin.gwt.curation.dto.DiseaseAnnotationModelDTO;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

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
        setEvidenceCodes();
    }

    @UiField
    Image loadingImage;
    @UiField
    SimpleErrorElement errorLabel;
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

    @UiHandler("fishSelectionBox")
    void onChangeFishSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.updateConditions();
        clearErrorMessage();
    }

    @UiHandler("environmentSelectionBox")
    void onChangeEnvironmentSelection(@SuppressWarnings("unused") ChangeEvent event) {
        clearErrorMessage();
    }

    @UiHandler("evidenceCodeSelectionBox")
    void onChangeEvidenceSelection(@SuppressWarnings("unused") ChangeEvent event) {
        clearErrorMessage();
    }

    public void clearErrorMessage() {
        errorLabel.setError("");
    }

    private void setEvidenceCodes() {
        evidenceCodeSelectionBox.addItem("TAS");
        evidenceCodeSelectionBox.addItem("IC");
    }

    protected void addFish(FishDTO fish, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (fish == null) {
            dataTable.setText(row, 0, "");
            return;
        }
        Anchor fishAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(fish.getHandle()), "/" + fish.getZdbID());
        fishAnchor.setTitle(fish.getZdbID());
        dataTable.setWidget(elementIndex + 1, 0, fishAnchor);
    }

    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    public void removeAllDataRows() {
        dataTable.resizeRows(1);
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, fishSelectionBox);
        dataTable.setWidget(lastRow, col++, environmentSelectionBox);
        dataTable.setText(lastRow, col, IS_A_MODEL_OF);
        dataTable.getCellFormatter().setStyleName(lastRow, col++, "bold");
        dataTable.setWidget(lastRow, col++, diseaseSelectionBox);
        dataTable.setWidget(lastRow, col++, evidenceCodeSelectionBox);
        dataTable.getRowFormatter().setStyleName(lastRow, "table-header");
        dataTable.setWidget(lastRow, col, addDiseaseModelButton);

    }

    protected void addEnvironment(EnvironmentDTO environment, int elementIndex) {
        if (environment == null) {
            dataTable.setText(elementIndex + 1, 1, "");
            return;
        }
        dataTable.setText(elementIndex + 1, 1, environment.getName());
    }

    protected void addIsModelOf(boolean show, int index) {
        int row = index + 1;
        if (show)
            dataTable.setText(row, 2, IS_A_MODEL_OF);
        else
            dataTable.setText(row, 2, "");
        dataTable.getCellFormatter().setStyleName(row, 2, "bold");
    }

    protected void addEvidence(String evidence, int elementIndex) {
        dataTable.setText(elementIndex + 1, 4, evidence);
    }

    protected void addDeleteButtonDisease(DiseaseAnnotationDTO disease, int elementIndex, ClickHandler clickHandler) {
        String title = "Delete Disease Model";
        String zdbID = disease.getZdbID();
        addDeleteButton(elementIndex, clickHandler, title, zdbID);
    }

    protected void addDeleteButtonFishModel(DiseaseAnnotationModelDTO model, int elementIndex, ClickHandler clickHandler) {
        String title = "Delete Fish Model from Annotation";
        String zdbID = model.getDat().getZdbID();
        addDeleteButton(elementIndex, clickHandler, title, zdbID);
    }

    private void addDeleteButton(int elementIndex, ClickHandler clickHandler, String title, String zdbID) {
        DeleteImage deleteImage;
        deleteImage = new DeleteImage(title);
        deleteImage.setTitle(deleteImage.getTitle() + ": " + zdbID);
        deleteImage.addClickHandler(clickHandler);
        dataTable.setWidget(elementIndex + 1, 5, deleteImage);
    }

    protected void addDisease(TermDTO disease, int elementIndex, ClickHandler clickHandler) {
        Hyperlink diseaseHyperlink = new Hyperlink(SafeHtmlUtils.fromTrustedString(disease.getTermName()), "diseaseName");
        diseaseHyperlink.setTitle(disease.getOboID());
        diseaseHyperlink.addClickHandler(clickHandler);
        dataTable.setWidget(elementIndex + 1, 3, diseaseHyperlink);
    }

    public void resetUI() {
        errorLabel.clearAllErrors();
        fishSelectionBox.setSelectedIndex(0);
        environmentSelectionBox.setSelectedIndex(0);
        diseaseSelectionBox.setSelectedIndex(0);
        evidenceCodeSelectionBox.setSelectedIndex(0);
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

    public void setPresenter(DiseaseModelPresenter presenter) {
        this.presenter = presenter;
    }

}
