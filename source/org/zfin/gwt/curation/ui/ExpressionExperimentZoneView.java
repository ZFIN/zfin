package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.ExpressionExperimentDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.ui.ToggleHyperlink;
import org.zfin.gwt.root.util.WidgetUtil;

/**
 * Expression Experiment zone
 */
public class ExpressionExperimentZoneView extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("ExpressionExperimentZoneView.ui.xml")
    interface MyUiBinder extends UiBinder<VerticalPanel, ExpressionExperimentZoneView> {
    }

    private ExpressionExperimentZonePresenter presenter;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    SimpleErrorElement errorElement;

    @UiField
    Image loadingImage;
    @UiField
    VerticalPanel expressionExperimentPanel;
    @UiField
    ToggleHyperlink showSelectExperiments;
    @UiField
    Grid dataTable;

    // construction zone
    @UiField
    CheckBox allExperimentsCheck;
    @UiField
    Button addButton;
    @UiField
    StringListBox geneList;
    @UiField
    StringListBox fishList;
    @UiField
    StringListBox environmentList;
    @UiField
    StringListBox assayList;
    @UiField
    StringListBox antibodyList;
    @UiField
    StringListBox genbankList;
    @UiField
    Button updateButton;
    @UiField
    ToggleHyperlink showSelectedAllLink;
    @UiField
    Hyperlink clearLink;

    public ExpressionExperimentZoneView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
        presenter.onShowHideClick(showHideToggle.isVisible());
    }

    @UiHandler("addButton")
    void onAddModel(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addExpressionExperiment();
    }

    @UiHandler("allExperimentsCheck")
    void onCheckAllExperiments(@SuppressWarnings("unused") ClickEvent event) {
        presenter.checkAllExperiments();
    }

    @UiHandler("updateButton")
    void onClickUpdateButton(@SuppressWarnings("unused") ClickEvent event) {
        clearError();
        presenter.updateExperiment();
    }

    @UiHandler("assayList")
    void onAssayChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onAssayChange();
    }

    @UiHandler("environmentList")
    void onEnvironmentChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("genbankList")
    void onGenbankChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("fishList")
    void onfishChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
    }

    @UiHandler("antibodyList")
    void onAntibodyChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onAntibodyChange();
    }

    @UiHandler("geneList")
    void onGeneChange(@SuppressWarnings("unused") ChangeEvent event) {
        clearError();
        presenter.onGeneChange();
    }


    protected void addGene(MarkerDTO gene, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        setRowStyle(row);
        if (gene == null) {
            dataTable.setText(row, 1, "");
            return;
        }
        dataTable.setText(row, 1, gene.getName());
    }

    public void addEnvironment(ExperimentDTO environment, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 3, environment.getName());
    }

    protected void addFish(String fishName, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 2, fishName);
    }

    protected void addAssay(String assayName, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 4, assayName);
    }

    protected void addDeleteButton(ExpressionExperimentDTO experiment, ClickHandler handler, int elementIndex) {
        int row = elementIndex + 1;
        Button delete;
        if (experiment.isUsedInExpressions())
            delete = new Button("X :" + experiment.getNumberOfExpressions());
        else
            delete = new Button("X");
        delete.setTitle(experiment.getExperimentZdbID());
        delete.addClickHandler(handler);

        dataTable.setWidget(row, 7, delete);
    }

    protected CheckBox addCheckBox(ExpressionExperimentDTO experiment, ClickHandler handler, int elementIndex) {
        int row = elementIndex + 1;
        CheckBox checkBox = new CheckBox();
        checkBox.setTitle(experiment.getExperimentZdbID());
        checkBox.addClickHandler(handler);
        dataTable.setWidget(row, 0, checkBox);
        return checkBox;
    }

    protected void addGenBank(String genbankName, int elementIndex) {
        int row = elementIndex + 1;
        if (genbankName != null)
            dataTable.setText(row, 6, genbankName);
        else
            dataTable.setText(row, 6, "");
    }

    protected void addAntibody(MarkerDTO antibody, int elementIndex) {
        int row = elementIndex + 1;
        if (antibody != null)
            dataTable.setText(row, 5, antibody.getName());
        else
            dataTable.setText(row, 5, "");
    }

    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }

    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, addButton);
        dataTable.setWidget(lastRow, col++, geneList);
        dataTable.setWidget(lastRow, col++, fishList);
        dataTable.setWidget(lastRow, col++, environmentList);
        dataTable.setWidget(lastRow, col++, assayList);
        dataTable.setWidget(lastRow, col++, antibodyList);
        dataTable.setWidget(lastRow, col++, genbankList);
        dataTable.setWidget(lastRow, col, updateButton);
    }


    public void setPresenter(ExpressionExperimentZonePresenter presenter) {
        this.presenter = presenter;
    }

    public void setError(String message) {
        errorElement.setText(message);
    }

    public void clearError() {
        errorElement.setError("");
    }


    public void showToggleLinks(boolean show) {
        clearLink.setVisible(show);
        showSelectedAllLink.setVisible(show);
    }


    protected void cleanupOnExit() {
        updateButton.setEnabled(true);
    }

    public StringListBox getGeneList() {
        return geneList;
    }

    public StringListBox getAntibodyList() {
        return antibodyList;
    }

    public StringListBox getAssayList() {
        return assayList;
    }

    public StringListBox getEnvironmentList() {
        return environmentList;
    }

    public StringListBox getFishList() {
        return fishList;
    }

    public StringListBox getGenbankList() {
        return genbankList;
    }

    public Image getLoadingImage() {
        return loadingImage;
    }

    public ShowHideToggle getShowHideToggle() {
        return showHideToggle;
    }

}
