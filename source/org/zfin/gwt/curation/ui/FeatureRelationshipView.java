package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.ui.ShowHideToggle;
import org.zfin.gwt.root.ui.SimpleErrorElement;
import org.zfin.gwt.root.ui.StringListBox;
import org.zfin.gwt.root.util.WidgetUtil;

public class FeatureRelationshipView extends Composite {

    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiTemplate("FeatureRelationshipView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, FeatureRelationshipView> {
    }

    private FeatureRelationshipPresenter presenter;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    StringListBox featureTypeList;
    @UiField
    StringListBox featureNameList;
    @UiField
    SimpleErrorElement errorLabel;
    @UiField
    StringListBox featureToAddList;
    @UiField
    StringListBox featureToAddRelationship;
    @UiField
    StringListBox featureToAddTarget;
    @UiField
    Label featureToAddType;
    @UiField
    Grid dataTable;
    @UiField
    Label message;
    @UiField
    Button addButton;

    public FeatureRelationshipView() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    @UiHandler("addButton")
    void onClickAddRelationship(@SuppressWarnings("unused") ClickEvent event) {
        presenter.addRelationship();
    }

    @UiHandler("featureToAddList")
    void onChangeFeatureSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.onFeatureSelectionChange(featureToAddList.getSelectedText());
    }

    @UiHandler("featureNameList")
    void onChangeFeatureNameSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.onFeatureNameFilterChange(featureNameList.getSelectedText());
    }

    @UiHandler("featureTypeList")
    void onChangeFeatureTypeSelection(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.onFeatureTypeFilterChange(featureTypeList.getSelectedText());
    }

    @UiHandler("featureToAddRelationship")
    void onChangeFeatureRelationship(@SuppressWarnings("unused") ChangeEvent event) {
        presenter.updateTargetGeneList(featureToAddList.getSelectedText(), featureToAddRelationship.getSelectedText());
    }

    private int currentGroupIndex;

    public void addFeatureCell(FeatureDTO feature, FeatureDTO lastFeature, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        String lastID = null;
        if (lastFeature != null)
            lastID = lastFeature.getZdbID();
        currentGroupIndex = WidgetUtil.setRowStyle(row, feature.getZdbID(), lastID, currentGroupIndex, dataTable);
        Anchor fishAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(feature.getName()), "/" + feature.getZdbID());
        fishAnchor.setTitle(feature.getZdbID());
        if (lastID == null || !feature.getZdbID().equals(lastID))
            dataTable.setWidget(row, 0, fishAnchor);
    }

    public void addFeatureTypeCell(FeatureDTO feature, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 1, feature.getFeatureType().getDisplay());
    }

    public void addFeatureRelationshipCell(String relationship, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setText(row, 2, relationship);
    }

    public void addTargetMarker(MarkerDTO marker, int elementIndex) {
        int row = elementIndex + 1;
        Anchor fishAnchor = new Anchor(SafeHtmlUtils.fromTrustedString(marker.getName()), "/" + marker.getZdbID());
        fishAnchor.setTitle(marker.getZdbID());
        dataTable.setWidget(row, 3, fishAnchor);
    }

    public void addDeletButton(Button deleteButton, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setWidget(row, 4, deleteButton);
    }


    private void setRowStyle(int row) {
        WidgetUtil.setAlternateRowStyle(row, dataTable);
    }


    protected void endTableUpdate() {
        int rows = dataTable.getRowCount() + 1;
        dataTable.resizeRows(rows);
        int lastRow = rows - 1;
        int col = 0;
        dataTable.setWidget(lastRow, col++, featureToAddList);
        dataTable.setWidget(lastRow, col++, featureToAddType);
        dataTable.setWidget(lastRow, col++, featureToAddRelationship);
        dataTable.setWidget(lastRow, col++, featureToAddTarget);
    }

    public void enableEntryFields() {
        addButton.setEnabled(true);
        featureToAddTarget.setEnabled(true);
        featureToAddList.setEnabled(true);
        featureToAddRelationship.setEnabled(true);

    }

    public void disableEntryFields() {
        addButton.setEnabled(false);
        featureToAddTarget.setEnabled(false);
        featureToAddList.setEnabled(false);
        featureToAddRelationship.setEnabled(false);
    }

    public void revertGUI() {
        featureToAddType.setText("");
        featureToAddRelationship.clear();
        featureToAddTarget.clear();
    }


    public void setPresenter(FeatureRelationshipPresenter presenter) {
        this.presenter = presenter;
    }
}


