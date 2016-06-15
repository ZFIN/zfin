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
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.ui.Revertible;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringTextBox;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

public class ExperimentAddView extends Composite  {

    public static final String STANDARD = "_Standard";
    public static final String GENERIC_CONTROL = "_Generic-Control";


    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private ExperimentAddPresenter presenter;

    @UiTemplate("ExperimentAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ExperimentAddView> {
    }

    public ExperimentAddView() {
        initWidget(binder.createAndBindUi(this));
        //setEvidenceCodes();
    }

    @UiField
    Image loadingImage;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    Grid dataTable;
    @UiField
    Button addConditionNameButton;
    @UiField
    Button deleteConditionButton;
    @UiField
    Button updateConditionNameButton;
    @UiField
    TextBox conditionNameAddBox;



    //@UiHandler("addConditionNameButton")
   /*void onAddModel(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addModelEvent();
    }

    /*@UiHandler("fishSelectionBox")
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



    private void setEvidenceCodes() {
        evidenceCodeSelectionBox.addItem("TAS");
        evidenceCodeSelectionBox.addItem("IC");
    }*/
    public void clearErrorMessage() {
        errorLabel.setError("");
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
        dataTable.setText(0,1,STANDARD);
        dataTable.setText(1,1,GENERIC_CONTROL);
        int rows = dataTable.getRowCount() + 3;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, updateConditionNameButton);
        dataTable.setWidget(lastRow, col++, conditionNameAddBox);
        dataTable.setWidget(lastRow, col, deleteConditionButton);

    }

    protected void addEnvironment(EnvironmentDTO environment, int elementIndex) {
        if (environment == null) {
            dataTable.setText(elementIndex + 1, 1, "");
            return;
        }
        dataTable.setText(elementIndex + 1, 1, environment.getName());
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
        conditionNameAddBox.setText("");

    }



    public SimpleErrorElement getErrorLabel() {
        return errorLabel;
    }


    public Image getLoadingImage() {
        return loadingImage;
    }

    public void setPresenter(ExperimentAddPresenter presenter) {
        this.presenter = presenter;
    }

}



