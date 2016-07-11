package org.zfin.gwt.curation.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.*;
import org.zfin.gwt.root.dto.ConditionDTO;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.ui.*;
import org.zfin.gwt.root.util.DeleteImage;
import org.zfin.gwt.root.util.WidgetUtil;

import java.util.Set;

public class ConditionAddView extends AbstractViewComposite {

    private static MyUiBinder binder = GWT.create(MyUiBinder.class);
    private ConditionAddPresenter presenter;

    @UiTemplate("ConditionAddView.ui.xml")
    interface MyUiBinder extends UiBinder<FlowPanel, ConditionAddView> {
    }

    public ConditionAddView() {
        initWidget(binder.createAndBindUi(this));
    }

    @UiField
    Image loadingImage;
    @UiField
    Grid dataTable;
    @UiField
    ShowHideToggle showHideToggle;
    @UiField
    Button createExperimentConditionButton;
    @UiField
    StringListBox experimentSelectionList;
    @UiField
    TermEntry zecoTermEntry;
    @UiField
    TermInfoComposite termInfoBox;
    @UiField
    TermEntry chebiTermEntry;
    @UiField
    Button resetButton;
    @UiField
    TermEntry goCcTermEntry;
    @UiField
    TermEntry aoTermEntry;
    @UiField
    TermEntry taxonTermEntry;
    @UiField
    Button copyExperimentConditionButton;
    @UiField
    StringListBox experimentCopyToSelectionList;
    @UiField
    HorizontalPanel copyControlsPanel;
    @UiField
    Grid controlTable;
    @UiField
    HorizontalPanel createButtonPanel;
    @UiField
    FlowPanel experimentSelectionPanel;

    @UiHandler("showHideToggle")
    void onClickShowHide(@SuppressWarnings("unused") ClickEvent event) {
        showHideToggle.toggleVisibility();
    }

    @UiHandler("resetButton")
    void onClickReset(@SuppressWarnings("unused") ClickEvent event) {
        presenter.resetGUI();
    }

    @UiHandler("createExperimentConditionButton")
    void onClickCreateCondition(@SuppressWarnings("unused") ClickEvent event) {
        loadingImage.setVisible(true);
        presenter.createCondition();
    }

    @UiHandler("copyExperimentConditionButton")
    void onClickCopyCondition(@SuppressWarnings("unused") ClickEvent event) {
        loadingImage.setVisible(true);
        presenter.copyConditions();
    }


    private int currentGroupIndex;

    protected void addCondition(ExperimentDTO experimentDTO, ConditionDTO dto, ConditionDTO lastCondition, int elementIndex) {
        dataTable.resizeRows(elementIndex + 2);
        int row = elementIndex + 1;
        int col = 0;
        dataTable.setText(row, col++, "");
        dataTable.getColumnFormatter().setWidth(0, "20px");
        dataTable.getColumnFormatter().setWidth(1, "150px");
        if (lastCondition == null || !lastCondition.getEnvironmentZdbID().equals(dto.getEnvironmentZdbID()))
            dataTable.setText(row, col++, experimentDTO.getName());
        else
            dataTable.setText(row, col++, "");
        dataTable.setText(row, col, dto.getName());

        String lastID = null;
        if (lastCondition != null)
            lastID = lastCondition.getEnvironmentZdbID();
        currentGroupIndex = WidgetUtil.setRowStyle(row, dto.getEnvironmentZdbID(), lastID, currentGroupIndex, dataTable);
    }

    public void addZeco() {
        controlTable.resizeRows(1);
        int row = 0;
        int column = 0;
        controlTable.setWidget(row, column++, experimentSelectionPanel);
        controlTable.setWidget(row++, column++, zecoTermEntry);
    }

    public void addTermEntry(TermEntry termEntry, int row) {
        controlTable.resizeRows(row + 1);
        int column = 0;
        controlTable.setText(row, column++, "");
        controlTable.setWidget(row++, column++, termEntry);
    }

    public void addControls(int row) {
        controlTable.resizeRows(row + 1);
        int column = 0;
        controlTable.setText(row, column++, "");
        controlTable.setWidget(row, column, createButtonPanel);
    }

    public void addCopyControlsPanel() {
        controlTable.resizeRows(1);
        controlTable.setWidget(0, 0, copyControlsPanel);
        controlTable.setText(0, 1, "");
        controlTable.setText(0, 2, "");
    }

    public void addDeleteButton(DeleteImage deleteImage, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setWidget(row, 3, deleteImage);
    }

    public void addCopyCheckBox(CheckBox checkBox, int elementIndex) {
        int row = elementIndex + 1;
        dataTable.setWidget(row, 0, checkBox);
    }


    public void emptyDataTable() {
        dataTable.resizeRows(2);
    }

    public void setPresenter(ConditionAddPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Set<IsDirtyWidget> getValueFields() {
        return null;
    }


}



